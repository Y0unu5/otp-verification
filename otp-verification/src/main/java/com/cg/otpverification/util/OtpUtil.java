package com.cg.otpverification.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtil {
    public String generateOtp(){
        Random random=new Random();
        int randomNum= random.nextInt(999999);
        String output=Integer.toString(randomNum);
        while (output.length()<6){
            output= "0" + output;
        }
        return output;
    }
}
