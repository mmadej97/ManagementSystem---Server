package data.jdbc;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class DBConfig {


    public static final String dbName = "Project9999";

    public static final String GENERATING_DATABASE_QUERY = "CREATE DATABASE IF NOT EXISTS " + dbName;


    public static final String GENERATING_TABLE_EMPLOYEES_QUERY = "CREATE TABLE IF NOT EXISTS EMPLOYEE(" +
                                                                  "EMPLOYEE_ID INT(4) AUTO_INCREMENT ," +
                                                                  "FIRST_NAME VARCHAR(16) NOT NULL," +
                                                                  "LAST_NAME VARCHAR(16) NOT NULL," +
                                                                  "USERNAME VARCHAR(32) NOT NULL," +
                                                                  "PASSWORD VARCHAR(32) NOT NULL," +
                                                                  "MANAGER_ID INT(4) ," +
                                                                  "SALARY FLOAT(7,2)," +
                                                                  "CONSTRAINT EMPLOYEE_PK PRIMARY KEY (EMPLOYEE_ID));";

    public static final String GENERATING_TABLE_SURVEYS_QUERY =   "CREATE TABLE IF NOT EXISTS SURVEY(" +
                                                                  "SURVEY_ID INT(4) AUTO_INCREMENT ," +
                                                                  "SURVEY_NAME VARCHAR(32) NOT NULL," +
                                                                  "CONSTRAINT SURVEY_PK PRIMARY KEY (SURVEY_ID));";



    public static final String GENERATING_TABLE_TASKS_QUERY =     "CREATE TABLE IF NOT EXISTS TASK(" +
                                                                  "TASK_ID INT(10)  AUTO_INCREMENT ," +
                                                                  "EMPLOYEE_FK INT(4)," +
                                                                  "SURVEY_FK INT(4)," +
                                                                  "TASK_DONE INT(1)," +
                                                                  "CONSTRAINT PRIMARY KEY (TASK_ID)," +
                                                                  "CONSTRAINT FOREIGN KEY (SURVEY_FK) REFERENCES SURVEY(SURVEY_ID)," +
                                                                  "CONSTRAINT FOREIGN KEY (EMPLOYEE_FK) REFERENCES EMPLOYEE(EMPLOYEE_ID));";

    public static final String GENERATING_TABLE_QUESTIONS_QUERY = "CREATE TABLE IF NOT EXISTS QUESTION(" +
                                                                  "QUESTION_ID INT(4) AUTO_INCREMENT," +
                                                                  "QUESTION_TEXT VARCHAR(50) NOT NULL," +
                                                                  "QUESTION_OPTIONS VARCHAR(50) NOT NULL," +
                                                                  "SURVEY_FK INT(4)," +
                                                                  "CONSTRAINT QUESTION_PK PRIMARY KEY (QUESTION_ID)," +
                                                                  "CONSTRAINT QUESTION_FK FOREIGN KEY (SURVEY_FK) REFERENCES SURVEY(SURVEY_ID));"   ;

    public static final String GENERATING_TABLE_ANSWERS_QUERY =   "CREATE TABLE IF NOT EXISTS ANSWER(" +
                                                                  "ANSWER_ID INT(10) AUTO_INCREMENT,"+
                                                                  "ANSWER_TEXT VARCHAR(100),"+
                                                                  "TASK_FK INT(10),"+
                                                                  "QUESTION_FK INT(4),"+
                                                                  "CONSTRAINT PRIMARY KEY (ANSWER_ID),"+
                                                                  "CONSTRAINT FOREIGN KEY (TASK_FK) REFERENCES TASK(TASK_ID),"+
                                                                  "CONSTRAINT FOREIGN KEY (QUESTION_FK) REFERENCES QUESTION(QUESTION_ID));";


    public static final String GET_EMPLOYEES_QUERY =    "SELECT * FROM EMPLOYEE";

    public static final String GET_TASKLIST_STATEMENT = "SELECT TASK_ID, SURVEY_NAME FROM SURVEY JOIN TASK ON SURVEY_ID = SURVEY_FK WHERE EMPLOYEE_FK = ? AND TASK_DONE <> 1";

    public static final String GET_QUESTIONS_STATEMENT = "SELECT QUESTION_TEXT FROM QUESTION  JOIN SURVEY ON SURVEY_FK = SURVEY_ID WHERE SURVEY_NAME = ?";

    public static final String GET_OPTIONS_STATEMENT =   "SELECT QUESTION_OPTIONS FROM QUESTION  JOIN SURVEY ON SURVEY_FK = SURVEY_ID WHERE SURVEY_NAME = ?";

    public static final String GET_TASK_NUMBER_STATMENT = "SELECT TASK_ID FROM TASK JOIN SURVEY ON SURVEY_FK = SURVEY_ID WHERE SURVEY_NAME = ? AND EMPLOYEE_FK = ?";

    public static final String GET_EMPLOYEE_LIST_QUERY = "SELECT EMPLOYEE_ID, LAST_NAME FROM EMPLOYEE";

    public static final String GET_SURVEY_LIST_QUERY = "SELECT SURVEY_ID, SURVEY_NAME FROM SURVEY";

    public static final String UPDATE_TASK_STATEMENT = "UPDATE TASK SET TASK_DONE = 1 WHERE TASK_ID = ?";

    public static final String GET_OPTION_COUNT_STATEMENT = "SELECT COUNT(*) FROM ANSWER JOIN QUESTION ON QUESTION_FK = QUESTION_ID JOIN SURVEY ON SURVEY_FK = SURVEY_ID WHERE ANSWER_TEXT = ? AND SURVEY_NAME =? AND QUESTION_ID = ?";

    public static void loadDriver(){

        System.out.print("Loading driver...");
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.print(" SUCCESS");

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exc) {

            System.out.print(" ERROR");
            System.exit(404);
        }
    }







    public static Connection connectToDatabase(String dbAdrress, String dbName){

        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "root");

        Connection connection = null;
        System.out.print("\nConnecting with database...");
        try{


            connection = DriverManager.getConnection("jdbc:mysql://" + dbAdrress + "/" + dbName + "?true&useLegacyDatetimeCode=false&serverTimezone=UTC", properties);
            System.out.print(" SUCCESS");

        }catch (SQLException exc){
            System.out.print(" ERROR");
            System.exit(405);
        }

        return connection;
    }



    public static Statement createStatement(Connection connection){

        Statement statement  = null;
        try {

            statement = connection.createStatement();
        }catch (SQLException exc){
            System.exit(406);
        }

        return statement;
    }


    public static void createDatabase(Statement statement){


        System.out.print("\nCreating database... ");
        try{


            statement.executeUpdate(GENERATING_DATABASE_QUERY);

            statement.executeUpdate("use " + dbName);

            statement.executeUpdate(GENERATING_TABLE_EMPLOYEES_QUERY);
            statement.executeUpdate(GENERATING_TABLE_SURVEYS_QUERY);
            statement.executeUpdate(GENERATING_TABLE_TASKS_QUERY);
            statement.executeUpdate(GENERATING_TABLE_QUESTIONS_QUERY);
            statement.executeUpdate(GENERATING_TABLE_ANSWERS_QUERY);
            System.out.println(" SUCCESS");
        }catch (SQLException exc){
            System.out.print(" ERROR");
            exc.printStackTrace();
            System.exit(407);
        }


    }



    public static int deleteQuery(Statement statement, String entityType, int index){

        try{

            String id = entityType + "_ID";

            String sql = "DELETE FROM " +
                        entityType +
                        " where " + id +  " = " + index;

            System.out.println(sql);
            return statement.executeUpdate(sql);

        }catch (SQLException exc){
            exc.printStackTrace();
            return -1;
        }

    }


    public static int saveQuery( Statement statement, Object savedObject) throws IllegalAccessException{

        try{


            Field[] fieldArr = savedObject.getClass().getDeclaredFields();
            String value = "";
            for(Field field : fieldArr){

                field.setAccessible(true);

                if(field.getName().equals(savedObject.getClass().getName().replace("data.entities.","").replace("Entity","").concat("_ID").toUpperCase()))
                    continue;

                if(field.getType() == String.class)
                    value +=  "'" + field.get(savedObject) + "'";

                if(field.getType() == Integer.class )
                    value += ((Integer)field.get(savedObject)).toString();

                if(field.getType() == Double.class)
                    value +=   ((Double)field.get(savedObject)).toString();

                if(field != fieldArr[fieldArr.length - 1])
                    value = value + ",";
            }



            String sql = "INSERT INTO " + savedObject.getClass().getName().replace("data.entities.","").replace("Entity","").toUpperCase() + " VALUES(null," + value + ");";
            System.out.println(sql);

            return statement.executeUpdate(sql);


        }catch (SQLException exc){
            exc.printStackTrace();
            return -1;
        }

    }


    public static PreparedStatement checkLoggingProperties(Connection connection){

        PreparedStatement preparedStatement = null;
        try{


            preparedStatement = connection.prepareStatement("SELECT * FROM EMPLOYEE WHERE USERNAME = ? AND PASSWORD = ?");


        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(408);
        }

        return preparedStatement;
    }


    public static PreparedStatement getQuestions(Connection connection){


        try{

            return connection.prepareStatement(GET_QUESTIONS_STATEMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }






    public static PreparedStatement getOptions(Connection connection){


        try{

            return connection.prepareStatement(GET_OPTIONS_STATEMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }


    public static PreparedStatement getTaskList(Connection connection){

        try{

            return connection.prepareStatement( GET_TASKLIST_STATEMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }



    public static PreparedStatement getOptionCount(Connection connection){

        try{

            return connection.prepareStatement(GET_OPTION_COUNT_STATEMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }


    public static PreparedStatement getTaskNumber(Connection connection){

        try{

            return connection.prepareStatement( GET_TASK_NUMBER_STATMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }


    public static PreparedStatement updateTask(Connection connection){

        try{

            return connection.prepareStatement( UPDATE_TASK_STATEMENT);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }



    public static ResultSet getSurveyList(Statement statement){

        try{

            return statement.executeQuery(GET_SURVEY_LIST_QUERY);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }

    public static ResultSet getEmployeeList(Statement statement){

        try{

            return statement.executeQuery(GET_EMPLOYEE_LIST_QUERY);

        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }


    public static ResultSet getQuery(Statement statement, String entityType){

        try{

            String sql = "SELECT * FROM " +
                         entityType;

            return statement.executeQuery(sql);


        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return null;
    }


    public static PreparedStatement getSurveys(Connection connection){
        PreparedStatement preparedStatement = null;
        try{


            preparedStatement = connection.prepareStatement("SELECT SURVEY_ID, TASK_ID, EMPLOYEE_FK,_SURVEY_NAME, QUESTION_ID, QUESTION_TEXT FROM TASKS NATURAL JOIN SURVEYS NATURAL JOIN QUESTIONS ");


        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(409);
        }

        return preparedStatement;







    }
}
