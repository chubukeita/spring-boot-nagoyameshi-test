package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CompanyService;

@WebMvcTest(CompanyController.class)
public class CompanyControllerUnitTest {

  @MockBean
  private CompanyService companyService;

  @Autowired
  private MockMvc mockMvc;

  private UserDetailsImpl userDetails;
  private Company company;

  @BeforeEach
  public void setUp() {
    User user = new User();
    Role role = new Role();
    role.setName("ROLE_FREE_MEMBER");
    user.setRole(role);
    userDetails = new UserDetailsImpl(user, List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

    company = new Company();
    company.setId(1);
    company.setName("株式会社サンプル");
    company.setPostalCode("100-0001");
    company.setAddress("東京都千代田区千代田1-1");
    company.setRepresentative("山田太郎");
    company.setEstablishmentDate("2020年1月1日");
    company.setCapital("1000万円");
    company.setNumberOfEmployees("50人");
    company.setBusiness("飲食店情報サービス");
  }

  @Test
  @Description("GET /company ログイン済みの場合は会社概要ページが正しく表示される")
  public void index_test_1() throws Exception {
    when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

    mockMvc.perform(get("/company").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("company/index"))
        .andExpect(model().attribute("company", company));

    verify(companyService).findFirstCompanyByOrderByIdDesc();
  }
}
