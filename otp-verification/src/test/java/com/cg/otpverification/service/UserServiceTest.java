package com.cg.otpverification.service;

import com.cg.otpverification.dto.LoginDto;
import com.cg.otpverification.dto.RegisterDto;
import com.cg.otpverification.entity.User;
import com.cg.otpverification.repository.UserRepository;
import com.cg.otpverification.util.EmailUtil;
import com.cg.otpverification.util.OtpUtil;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private OtpUtil otpUtil;

  @Mock
  private EmailUtil emailUtil;

  @InjectMocks
  UserService userService= new UserServiceImpl();

  @Test
  public void testRegister() throws MessagingException {
    RegisterDto registerDto = new RegisterDto();
    registerDto.setName("Test User");
    registerDto.setEmail("test@example.com");
    registerDto.setPassword("password");
    String otp = "123456";
    when(otpUtil.generateOtp()).thenReturn(otp);
    userService.register(registerDto);
    verify(emailUtil).sendOtpEmail(eq(registerDto.getEmail()), eq(otp));
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void testVerifyAccount_ValidOtp() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setOtp("123456");
    user.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(30));
    user.setActive(false);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    assertEquals("OTP verified you can login", userService.verifyAccount(user.getEmail(), user.getOtp()));
    assertEquals(true, user.isActive());
    verify(userRepository).save(user);
  }

  @Test
  public void testVerifyAccount_InvalidOtp() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setOtp("123456");
    user.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(30));
    user.setActive(false);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    assertEquals("Please regenerate otp and try again", userService.verifyAccount(user.getEmail(), "654321"));
    assertEquals(false, user.isActive());
    verify(userRepository, never()).save(user);
  }

  @Test
  public void testRegenerateOtp() throws MessagingException {
    User user = new User();
    user.setEmail("test@example.com");
    String otp = "123456";
    when(otpUtil.generateOtp()).thenReturn(otp);
    userService.regenerateOtp(user.getEmail());
    verify(emailUtil).sendOtpEmail(eq(user.getEmail()), eq(otp));
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void testLogin_ValidCredentials() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    user.setActive(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail(user.getEmail());
    loginDto.setPassword(user.getPassword());
    assertEquals("Login successful", userService.login(loginDto));
  }

  @Test
  public void testLogin_IncorrectPassword() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    user.setActive(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail(user.getEmail());
    loginDto.setPassword("wrong_password");
    assertEquals("Password is incorrect", userService.login(loginDto));
  }

  @Test
  public void testLogin_AccountNotVerified() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    user.setActive(false);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail(user.getEmail());
    loginDto.setPassword(user.getPassword());
    assertEquals("your account is not verified", userService.login(loginDto));
  }
}
