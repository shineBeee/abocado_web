package com.cccr.abocado.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpSession;

import com.cccr.abocado.dto.basic.*;
import com.cccr.abocado.dto.hospital.PatientHosConVo;
import com.cccr.abocado.dto.session.*;
import com.cccr.abocado.dto.user.UserBloodConVo;
import com.cccr.abocado.service.mypage.MypageHosService;
import com.cccr.abocado.service.mypage.MypageUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.cccr.abocado.dto.soyoung.GetAllAssets;
import com.cccr.abocado.dto.soyoung.HosPaper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.checkerframework.checker.units.qual.g;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyPageController {

    public static Gateway connect() throws Exception {
        // Load a file system based wallet for managing identities.
        System.out.println("connect start\n");
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCPs
        Path networkConfigPath = Paths.get("config", "connection-redcross.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);
        System.out.println("connect end\n");
        return builder.connect();
    }

    public static Gateway gateway = null;

    @Autowired
    MypageUserService mypageUserService;

    @Autowired
    MypageHosService mypageHosService;

    // 개인 유저 마이페이지 컨트롤러
    // 마이페이지의 3개의 카테고리별로 갯수 체크

    @RequestMapping("mypagePersonal")
    public String mypagePersonal(HttpSession session, Model model) {
        
        try {
            EnrollAdmin.main(null);
            RegisterUser.main(null);
        } catch (Exception e) {
            System.err.println(e);
        }

        if (gateway == null) {
            try {
                gateway = connect();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        SessionUserVo sessionInfo = (SessionUserVo) session.getAttribute("sessionUserInfo");
        String userIdx = sessionInfo.getUserIdx();
        ArrayList<GetAllAssets> userList = new ArrayList<GetAllAssets>();
        ArrayList<GetAllAssets> uuList = new ArrayList<GetAllAssets>();
        ArrayList<GetAllAssets> unuList = new ArrayList<GetAllAssets>();
        ArrayList<UserBloodConVo> userBloodList = mypageUserService.getUserBloodList(userIdx);

        int totalCount = 0;
        int trueCount = 0;
        int falseCount =0;


        // HLF

        // get the network and contract
        Network network = gateway.getNetwork("mychannel");
        Contract contract = network.getContract("basic");

        byte[] result1 = null;
        byte[] result2 = null;
        byte[] result3 = null;

        try {
            result1 = contract.evaluateTransaction("UserPaper", sessionInfo.getUserName());
            result2 = contract.evaluateTransaction("UserUsedPaper",sessionInfo.getUserName(),"T");
            result3 = contract.evaluateTransaction("UserUsedPaper",sessionInfo.getUserName(),"F");       
        } catch (ContractException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
             

        String str1 = new String(result1);
        String str2 = new String(result2);
        String str3 = new String(result3);

        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray1 = (JsonArray) jsonParser.parse(str1);
        JsonArray jsonArray2 = (JsonArray) jsonParser.parse(str2);
        JsonArray jsonArray3 = (JsonArray) jsonParser.parse(str3);

        System.out.println("Evaluate Transaction: UserPaper, result: " + new String(result1));  

        for (int i=0; i<jsonArray1.size();i++){
            JsonObject object = (JsonObject) jsonArray1.get(i);
            String blood = object.get("bloodIdx").getAsString();
            String user = object.get("userIdx").getAsString();
            String patient = object.get("patientIdx").getAsString();
            String hos = object.get("hosIdx").getAsString();
            String usage = object.get("bloodUsage").getAsString();
            String bdate = object.get("bloodDate").getAsString();
            String ddate = object.get("donateDate").getAsString();

            System.out.println(blood + user + patient + hos + usage + bdate + ddate);

            GetAllAssets up1 = new GetAllAssets(blood, user, patient, hos, usage, bdate, ddate);
            totalCount++;
            userList.add(up1);
        }

        System.out.println("Evaluate Transaction: UserNotUsedPaper, result: " + new String(result2));  

        for (int i=0; i <jsonArray2.size(); i++){
            JsonObject object = (JsonObject) jsonArray2.get(i);
            String blood = object.get("bloodIdx").getAsString();
            String user = object.get("userIdx").getAsString();
            String patient = object.get("patientIdx").getAsString();
            String hos = object.get("hosIdx").getAsString();
            String usage = object.get("bloodUsage").getAsString();
            String bdate = object.get("bloodDate").getAsString();
            String ddate = object.get("donateDate").getAsString();

            System.out.println(blood + user + patient + hos + usage + bdate + ddate);

            GetAllAssets up2 = new GetAllAssets(blood, user, patient, hos, usage, bdate, ddate);
            falseCount++;
            uuList.add(up2);
        }

        System.out.println("Evaluate Transaction: UserUsedPaper, result: " + new String(result3));

        for (int i=0; i <jsonArray3.size(); i++){
            JsonObject object = (JsonObject) jsonArray3.get(i);
            String blood = object.get("bloodIdx").getAsString();
            String user = object.get("userIdx").getAsString();
            String patient = object.get("patientIdx").getAsString();
            String hos = object.get("hosIdx").getAsString();
            String usage = object.get("bloodUsage").getAsString();
            String bdate = object.get("bloodDate").getAsString();
            String ddate = object.get("donateDate").getAsString();

            System.out.println(blood + user + patient + hos + usage + bdate + ddate);

            GetAllAssets up3 = new GetAllAssets(blood, user, patient, hos, usage, bdate, ddate);
            trueCount++;
            unuList.add(up3);
        } 

        model.addAttribute("userList", userList);
        model.addAttribute("uuList", uuList);
        model.addAttribute("unuList", unuList);

        model.addAttribute("userBloodList", userBloodList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("trueCount", trueCount);
        model.addAttribute("falseCount", falseCount);

        return "mypagePersonal";
    }

    @RequestMapping("mypageHospital")
    public String mypageHopital(HttpSession session, Model model) throws ParseException {

        SessionHosVo hosInfo = (SessionHosVo)session.getAttribute("sessionHosInfo");
        ArrayList<HosPaper> hosList = new ArrayList<HosPaper>();
        ArrayList<PatientHosConVo> patientHosConVos = mypageHosService.getHospitalInfo(hosInfo.getHosIdx());
        ArrayList<BasicPatientVo> patientList = mypageHosService.getPatientInfo(hosInfo.getHosIdx());
        
        ArrayList<BasicPatientVo> endedPatient = new ArrayList<>();
        ArrayList<BasicPatientVo> waitingPatient = new ArrayList<>();

        int totalCount = 0;
        int waitCount;
        int endCount;

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        //오늘 날짜와 포멧변경
        Date today = new Date();
        String changeTodayDay = transFormat.format(today);
        
        //오늘 날짜와 환자 등록 마지막날 비교
        for (BasicPatientVo vo : patientList) {
            Date patientEndDate = transFormat.parse(vo.getPatientEnddate());
            Date compareDate = transFormat.parse(changeTodayDay);

           
            int compareResult = patientEndDate.compareTo(compareDate);
            
            if(compareResult > 0){
                waitingPatient.add(vo);
            }
            else{  
                endedPatient.add(vo);
            }
        }

        //HLF
        try {
            EnrollAdmin.main(null);
                RegisterUser.main(null);
        } catch (Exception e) {
                System.err.println(e);
        }
        try (Gateway gateway = connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("basic");

            byte[] result;

            System.out.println("Submit Transaction: InitLedger creates the initial set of assets on the ledger.");
            contract.submitTransaction("InitLedger");

            result = contract.evaluateTransaction("HosPaper",hosInfo.getHosName());
            System.out.println("Evaluate Transaction: HosPaper, result: " + new String(result));              

            String str = new String(result);

            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = (JsonArray) jsonParser.parse(str);

            for (int i=0; i<jsonArray.size();i++){
                JsonObject object = (JsonObject) jsonArray.get(i);
                String blood = object.get("bloodIdx").getAsString();
                String patient = object.get("patientIdx").getAsString();
                String hos = object.get("hosIdx").getAsString();
                String usage = object.get("bloodUsage").getAsString();
                String ddate = object.get("donateDate").getAsString();

                System.out.println(blood + patient + hos + usage + ddate);

                HosPaper hp = new HosPaper(blood, patient, hos, usage, ddate);
                totalCount++;
                hosList.add(hp);
            }
        }
        catch(Exception e){
                System.err.println(e);
        }
                


        waitCount = waitingPatient.size();
        endCount = endedPatient.size();
        
        model.addAttribute("hosList", hosList);
        
        model.addAttribute("mypageHosPatientList", patientHosConVos);
        model.addAttribute("waitingPatientList", waitingPatient);
        model.addAttribute("endedPatientList", endedPatient);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("waitCount", waitCount);
        model.addAttribute("endCount", endCount);

        return "mypageHospital";

    }
    
    @RequestMapping("mypageGov")
    public String hlf(HttpSession session,Model model) {
        // enrolls the admin and registers the user

        int totalCount = 0;
        int usedCount = 0;
        SessionGovVo sessionInfo =  (SessionGovVo) session.getAttribute("sessionGovInfo");
        ArrayList<GetAllAssets> gaaList = new ArrayList<GetAllAssets>();
        ArrayList<GetAllAssets> uaaList = new ArrayList<GetAllAssets>();
        try {
                EnrollAdmin.main(null);
                RegisterUser.main(null);
        } catch (Exception e) {
                System.err.println(e);
        }

        // connect to the network and invoke the smart contract
        try (Gateway gateway = connect()) {

                // get the network and contract
                Network network = gateway.getNetwork("mychannel");
                Contract contract = network.getContract("basic");

                byte[] result1, result2;

                System.out.println("\n");
                result1 = contract.evaluateTransaction("GetAllAssets");
                result2 = contract.evaluateTransaction("ReadUsedPaper");

                String str1 = new String(result1);
                String str2 = new String(result2);

                JsonParser jsonParser = new JsonParser();
                JsonArray jsonArray1 = (JsonArray) jsonParser.parse(str1);
                JsonArray jsonArray2 = (JsonArray) jsonParser.parse(str2);
                

                System.out.println("Evaluate Transaction: GetAllAssets, result: " + new String(result1));

                for (int i=0; i<jsonArray1.size();i++){
                        JsonObject object = (JsonObject) jsonArray1.get(i);
                        String blood = object.get("bloodIdx").getAsString();
                        String user = object.get("userIdx").getAsString();
                        String patient = object.get("patientIdx").getAsString();
                        String hos = object.get("hosIdx").getAsString();
                        String usage = object.get("bloodUsage").getAsString();
                        String bdate = object.get("bloodDate").getAsString();
                        String ddate = object.get("donateDate").getAsString();

                        GetAllAssets gaa = new GetAllAssets(blood, user, patient, hos, usage, bdate, ddate);
                        gaaList.add(gaa);
                        totalCount++;
                }

                System.out.println("Evaluate Transaction: GetAllAssets, result: " + new String(result2));

                for (int i=0; i<jsonArray2.size();i++){
                        JsonObject object = (JsonObject) jsonArray2.get(i);
                        String blood = object.get("bloodIdx").getAsString();
                        String user = object.get("userIdx").getAsString();
                        String patient = object.get("patientIdx").getAsString();
                        String hos = object.get("hosIdx").getAsString();
                        String usage = object.get("bloodUsage").getAsString();
                        String bdate = object.get("bloodDate").getAsString();
                        String ddate = object.get("donateDate").getAsString();

                        GetAllAssets uaa = new GetAllAssets(blood, user, patient, hos, usage, bdate, ddate);
                        uaaList.add(uaa);
                        usedCount++;
                }

        }
        catch(Exception e){
                System.err.println(e);
        }
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("usedCount", usedCount);
        model.addAttribute("gaaList", gaaList);
        model.addAttribute("uaaList", uaaList);
        return "mypageGov";
    }
}
