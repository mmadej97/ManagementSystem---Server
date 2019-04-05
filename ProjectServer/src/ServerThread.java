import data.entities.*;
import data.jdbc.DBConfig;


import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerThread implements Callable<String> {


    private Socket clientSocket;
    private Connection connnection;

    public ServerThread(Socket clientSocket, Connection connnection) {
        this.clientSocket = clientSocket;
        this.connnection = connnection;
    }


    public void sendObjectFromDB(Statement statement, ObjectInputStream reader , ObjectOutputStream writer, String entityType){


        System.out.println(entityType);
        ResultSet result =  DBConfig.getQuery(statement, entityType);

        try {
            while(result.next()) {
                Entity entity = (Entity) Class.forName("data.entities." + entityType + "Entity").newInstance();
                writer.writeObject(new GenericEntity<Entity>(entity).initializeEntity(result));
            }

            writer.writeObject(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getTaskList(EmployeeEntity user){


        try {
            PreparedStatement preparedStatement =  DBConfig.getTaskList(connnection);
            preparedStatement.setInt(1, user.getEMPLOYEE_ID().intValue());
            ResultSet result = preparedStatement.executeQuery();
            List<String> taskSurveys = new ArrayList<>();
            while (result.next()){
                taskSurveys.add("ID:" + result.getInt(1)+ "  " + result.getString(2) );
            }
            return taskSurveys;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    public List<String> getQuestions(String surveyName){


        List<String> questionList = null;
        try {
            PreparedStatement preparedStatement = DBConfig.getQuestions(connnection);
            preparedStatement.setString(1, surveyName);
            ResultSet result = preparedStatement.executeQuery();
            questionList = new ArrayList<>();

            while (result.next()){

                questionList.add(result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return questionList;
    }


    public List<String> getSurveyList(Statement statement){

        ResultSet result = DBConfig.getSurveyList(statement);
        List<String> surveyList = new ArrayList<>();
        try {
            while (result.next()){

                int  tempInt = result.getInt(1);
                String tempString = result.getString(2);
                surveyList.add("ID: " + tempInt + " Name: " + tempString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  surveyList;


    }


    public int updateTask(Connection connection, Integer taskID){

        PreparedStatement preparedStatement = DBConfig.updateTask(connection);
        int rowModifiedCount = 0;
        try {
            preparedStatement.setInt(1, taskID);
            rowModifiedCount = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return rowModifiedCount;
    }


    public int getOptionCount(String optionName, String surveyName, Integer questionId){

        PreparedStatement preparedStatement = DBConfig.getOptionCount(connnection);
        int count = 0;
        try {
            preparedStatement.setString(1, optionName);
            preparedStatement.setString(2, surveyName);
            preparedStatement.setInt(3, questionId);

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                count = result.getInt(1);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return count;
    }

    public List<String> getEmployeeList(Statement statement){

        ResultSet result = DBConfig.getEmployeeList(statement);
        List<String> employeeList = new ArrayList<>();
        try {
            while (result.next()){

                int  tempInt = result.getInt(1);
                String tempSring = result.getString(2);
                employeeList.add("ID: " + tempInt + " Surname: " + tempSring);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  employeeList;

    }

    public List<String> getOptions(String surveyName){


        List<String> optionsList = null;
        try {
            PreparedStatement preparedStatement = DBConfig.getOptions(connnection);
            preparedStatement.setString(1, surveyName);
            ResultSet result = preparedStatement.executeQuery();
            optionsList = new ArrayList<>();

            while (result.next()){
                System.out.println(result.getString(1));
                optionsList.add(result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return optionsList;
    }

    public EmployeeEntity logging(ObjectInputStream reader, ObjectOutputStream writer){

        EmployeeEntity loggingEmployeeEntity = null;
        try {
             loggingEmployeeEntity = (EmployeeEntity) reader.readObject();
            System.out.println("Logging attempt by username: " + loggingEmployeeEntity.getUSERNAME());

            //SELECT * FROM EMPLOYEES WHERE USERNAME = ? AND PASSWORD = ?
            PreparedStatement checkLoggingStatement =  DBConfig.checkLoggingProperties(connnection);
            checkLoggingStatement.setString(1, loggingEmployeeEntity.getUSERNAME());
            checkLoggingStatement.setString(2, loggingEmployeeEntity.getPASSWORD());
            ResultSet checkLoginResult = checkLoggingStatement.executeQuery();



            try {
                while(checkLoginResult.next()) {
                   loggingEmployeeEntity = new GenericEntity<>(new EmployeeEntity()).initializeEntity(checkLoginResult);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException  |ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return loggingEmployeeEntity;
    }


    public void deleteObject(Statement statement, ObjectInputStream reader, String entityType, int index){

            DBConfig.deleteQuery(statement, entityType, index ) ;

    }



    @Override
    public String call() throws Exception{

        ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
        Statement statement = DBConfig.createStatement(connnection);

        EmployeeEntity loggingEmployeeEntity = logging(reader, writer);

        if(loggingEmployeeEntity.getEMPLOYEE_ID() != null) {
            if (loggingEmployeeEntity.getMANAGER_ID() != 0) {

                System.out.println("User logged lastname: " + loggingEmployeeEntity.getLAST_NAME());
                writer.write(200);
                writer.writeObject(loggingEmployeeEntity);


                String str = "";
                while(true){

                    str = (String) reader.readObject();

                    if(str.equals("get:taskList")){
                        List<String> taskList = getTaskList(loggingEmployeeEntity);
                        for(String temp : taskList) {
                            if(temp != null)
                                writer.writeObject(new String(temp));
                        }
                        writer.writeObject(new String("end"));
                    }


                    if(str.startsWith("get:Questions:")){

                        List<String> questionList = getQuestions(str.replace("get:Questions:",""));
                        for(String temp :  questionList)
                            writer.writeObject(temp);
                        writer.writeObject(new String("end"));
                    }


                    if(str.startsWith("get:Options:")){

                        List<String> optionList = getOptions(str.replace("get:Options:",""));
                        for(String temp :  optionList)
                            writer.writeObject(temp);

                        writer.writeObject(new String("end"));
                    }

                    if(str.startsWith("save:Answer:")){

                        PreparedStatement preparedStatement = DBConfig.getTaskNumber(connnection);
                        preparedStatement.setString(1,str.replace("save:Answer:",""));
                        preparedStatement.setInt(2, loggingEmployeeEntity.getEMPLOYEE_ID());
                        ResultSet result = preparedStatement.executeQuery();
                        Integer taskNumber = null;
                        while (result.next()){
                            taskNumber = result.getInt(1);
                        }

                        String temp = (String) reader.readObject();
                        while(!temp.equals("end")) {

                            System.out.println(temp);
                            String[] tempArr = temp.split(":");

                            DBConfig.saveQuery(statement, new AnswerEntity( 0,tempArr[1], taskNumber, Integer.valueOf(tempArr[0])));
                            temp = (String) reader.readObject();
                        }
                    }

                    if(str.startsWith("update:task:")){
                        updateTask(connnection, Integer.valueOf(str.replace("update:task:","")));
                    }

                    if(str.equals("end:program")){
                        break;
                    }

                }



            } else {
                System.out.println("Admin logged lastname: " + loggingEmployeeEntity.getLAST_NAME());
                writer.write(201);
                writer.writeObject(loggingEmployeeEntity);

                String str = "";
                while(true) {
                    str = (String) reader.readObject();
                    System.out.println(str);


                    //saving entity to database
                    if (str.startsWith("save:")) {


                        if (str.replace("save:", "").equals("Answer")) {
                            AnswerEntity answerEntity = (AnswerEntity) reader.readObject();
                            DBConfig.saveQuery(statement, answerEntity);
                        }

                        if (str.replace("save:", "").equals("Employee")) {
                            EmployeeEntity employeeEntity = (EmployeeEntity) reader.readObject();
                            DBConfig.saveQuery(statement, employeeEntity);
                        }

                        if (str.replace("save:", "").equals("Question")) {
                            QuestionEntity questionEntity = (QuestionEntity) reader.readObject();
                            DBConfig.saveQuery(statement, questionEntity);
                        }

                        if (str.replace("save:", "").equals("Survey")) {
                            SurveyEntity surveyEntity = (SurveyEntity) reader.readObject();
                            DBConfig.saveQuery(statement, surveyEntity);
                        }

                        if (str.replace("save:", "").equals("Task")) {
                            TaskEntity taskEntity = (TaskEntity) reader.readObject();
                            DBConfig.saveQuery(statement, taskEntity);
                        }
                    }

                    //getting entity from database
                    if (str.startsWith("get:")) {
                        sendObjectFromDB(statement, reader, writer, str.replace("get:", ""));
                    }


                    //deleting from database
                    if (str.startsWith("delete:")) {
                        Integer index = (Integer) reader.readObject();
                        while (!index.equals(0)) {
                            deleteObject(statement, reader, str.replace("delete:", ""), index);
                            index = (Integer) reader.readObject();
                        }
                    }


                    if(str.startsWith("take:employeeList")){
                        List<String> employeeList = getEmployeeList(statement);
                        for(String temp : employeeList) {
                            if(temp != null)
                                writer.writeObject(new String(temp));
                        }
                        writer.writeObject(new String("end"));

                    }


                    if(str.startsWith("take:surveyList")){
                        List<String> surveyList = getSurveyList(statement);
                        for(String temp : surveyList) {
                            if(temp != null)
                                writer.writeObject(new String(temp));
                        }
                        writer.writeObject(new String("end"));

                    }




                    if(str.startsWith("take:Options:")){

                        List<String> optionList = getOptions(str.replace("take:Options:",""));
                        for(String temp :  optionList)
                            writer.writeObject(temp);

                        writer.writeObject(new String("end"));
                    }

                    if(str.startsWith("take:OptionsCount:")){

                        String[] arr = str.replace("take:OptionsCount:","").split(":");
                        writer.writeObject(new Integer(getOptionCount(arr[0], arr[1], Integer.valueOf(arr[2]) )));

                    }


                    if(str.startsWith("take:Questions:")){

                        List<String> questionList = getQuestions(str.replace("take:Questions:",""));
                        for(String temp :  questionList)
                            writer.writeObject(temp);
                        writer.writeObject(new String("end"));
                    }

                    if(str.equals("end:program")){
                        break;
                    }

                }

            }

        }else
            writer.write(-1);


        reader.close();
        writer.close();
        clientSocket.close();

        return "Client: " + clientSocket.getInetAddress().getHostName() + " ended connection with server";
    }

}
