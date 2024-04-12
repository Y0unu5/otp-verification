package com.cg.otpverification.service;

import com.cg.otpverification.dto.LoginDto;
import com.cg.otpverification.dto.RegisterDto;

public interface UserService {
    String register(RegisterDto registerDto);
    String verifyAccount(String email, String otp);
    String regenerateOtp(String email);
    String login(LoginDto loginDto);

    String forgetPassword(String email);

    String setPassword(String email, String newPassword);
}
