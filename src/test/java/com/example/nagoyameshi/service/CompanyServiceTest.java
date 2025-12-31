package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

  @Mock
  private CompanyRepository companyRepository;

  @InjectMocks
  private CompanyService companyService;

  @Test
  @Description("findFirstCompanyByOrderByIdDesc_ID最大の会社情報を取得できること")
  void findFirstCompanyByOrderByIdDesc_test_1() {
    Company company = new Company();
    company.setId(1);
    company.setName("テスト会社");
    company.setPostalCode("1010022");
    company.setAddress("東京都千代田区テスト1-1-1");
    company.setRepresentative("代表 太郎");
    company.setEstablishmentDate("2000年1月1日");
    company.setCapital("1000万円");

    when(companyRepository.findFirstByOrderByIdDesc()).thenReturn(company);

    Company result = companyService.findFirstCompanyByOrderByIdDesc();

    assertNotNull(result);
    assertEquals(1, result.getId());
    assertEquals("テスト会社", result.getName());
    verify(companyRepository, times(1)).findFirstByOrderByIdDesc();
  }

  @Test
  @Description("updateCompany_フォーム内容で会社情報を更新できること")
  void updateCompany_test_1() {
    CompanyEditForm form = new CompanyEditForm(
        "更新後会社名",
        "1234567",
        "更新後住所",
        "更新後代表者",
        "2020年5月1日",
        "2000万円",
        "事業内容：テスト",
        "20名");

    Company company = new Company();
    company.setId(1);

    companyService.updateCompany(form, company);

    assertEquals("更新後会社名", company.getName());
    assertEquals("1234567", company.getPostalCode());
    assertEquals("更新後住所", company.getAddress());
    assertEquals("更新後代表者", company.getRepresentative());

    verify(companyRepository, times(1)).save(company);
  }
}
