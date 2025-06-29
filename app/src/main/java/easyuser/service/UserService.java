package easyuser.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import easyuser.domain.User;
import easyuser.domain.UserContainer;

@Service
public class UserService {

    private static final String USER_GROUP_FETCH_URL = "https://api-ca.hosted.exlibrisgroup.com/almaws/v1/conf/code-tables/UserGroups?scope=01OCUL_BU&apikey=";
    private static final String USER_STORE_URL = "https://api-ca.hosted.exlibrisgroup.com/almaws/v1/users?social_authentication=false&send_pin_number_letter=false&registration_rules=false&apikey=";

    private static final String USER_BODY_JSON_STR = """
                {
                    "record_type": {
                        "value": "PUBLIC"
                    },
                    "primary_id": "%s",
                    "first_name": "%s",
                    "last_name": "%s",
                    "created_by": "exl_impl",
                    "created_date": "%s",
                    "user_group": {
                        "value": "%s"
                    },
                    "expiry_date": "%s",
                    "account_type": {
                        "value": "INTERNAL"
                    },
                    "password": "Library2025!",
                    "force_password_change": "TRUE",
                    "status": {
                        "value": "ACTIVE"
                    },
                    "status_date": "%s",
                    "contact_info": {
                        "email": [
                            {
                                "preferred": true,
                                "email_address": "%s",
                                "description": "string",
                                "email_type": [
                                    {
                                        "value": "personal"
                                    }
                                ]
                            }
                        ]
                    },
                    "user_role": [
                        {
                            "status": {
                                "value": "ACTIVE",
                                "desc": "Active"
                            },
                            "scope": {
                                "value": "01OCUL_BU",
                                "desc": "Brock University"
                            },
                            "role_type": {
                                "value": "200",
                                "desc": "Patron"
                            },
                            "parameter": []
                        },
                        {
                            "status": {
                              "value": "ACTIVE",
                              "desc": "Active"
                            },
                            "scope": {
                              "value": "HAMILTON",
                              "desc": "Instructional Resource Centre, Burlington Campus"
                            },
                            "role_type": {
                                "value": "32",
                                "desc": "Circulation Desk Operator"
                            },
                            "parameter": [
                              {
                                "type": {
                                  "value": "CirculationDesk"
                                },
                                "scope": {
                                  "value": "HAMILTON",
                                  "desc": "Instructional Resource Centre, Burlington Campus"
                                },
                                "value": {
                                  "value": "DEFAULT_CIRC_DESK",
                                  "desc": "Burlington Desk"
                                }
                              },
                              {
                                "type": {
                                  "value": "CantEditRestrictedUsers"
                                },
                                "scope": {
                                  "value": "HAMILTON",
                                  "desc": "Instructional Resource Centre, Burlington Campus"
                                },
                                "value": {
                                  "value": "false"
                                }
                              }
                            ]
                          }
                    ]
                }
            """;

    @Value("${alma.api.key}")
    String apiKey;

    @Cacheable("almaUserGroupsJson")
    public String fetchUserGroupsJsonStr() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = USER_GROUP_FETCH_URL + apiKey;

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println(response.getHeaders().get("X-Exl-Api-Remaining"));

        return new Gson().toJson(new GsonJsonParser().parseMap(response.getBody()).get("row"));
    }

    public List<User> performSanityCheckAndStore(UserContainer userContainer) throws InterruptedException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = df.format(new Date()) + "Z";
        String expiryDateStr = df.format(userContainer.getExpiryDate()) + "Z";

        String url = USER_STORE_URL + apiKey;

        List<User> storeFailedUsers = new ArrayList<>();

        processPotentialUsersFromExcel(userContainer);

        for (User user : userContainer.getUsers()) {
            String jsonBody = String.format(USER_BODY_JSON_STR,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    todayStr,
                    userContainer.getUserGroup(),
                    expiryDateStr,
                    todayStr,
                    user.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().value() != 200) {
                    storeFailedUsers.add(user);
                }
            } catch (Exception e) {
                storeFailedUsers.add(user);
            }

            Thread.sleep(250);
        }

        return storeFailedUsers;
    }

    private void processPotentialUsersFromExcel(UserContainer userContainer) {
        if (userContainer.getExcelFile().getSize() == 0) {
            return;
        }

        List<User> users = new ArrayList<>();
        try {
            Workbook xlWorkbook = new XSSFWorkbook(userContainer.getExcelFile().getInputStream());
            Sheet sheet = xlWorkbook.getSheetAt(0);

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();
            for (int index = firstRow + 1; index <= lastRow; index++) {
                Row row = sheet.getRow(index);

                try {
                    String firstName = row.getCell(0).getStringCellValue();
                    String lastName = row.getCell(1).getStringCellValue();
                    String email = row.getCell(2).getStringCellValue();

                    users.add(new User(firstName, lastName, email));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            userContainer.setUsers(users);
            xlWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
