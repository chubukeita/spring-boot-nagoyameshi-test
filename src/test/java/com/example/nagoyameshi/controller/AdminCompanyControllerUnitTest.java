package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.service.CompanyService;

@WebMvcTest(AdminCompanyController.class)
public class AdminCompanyControllerUnitTest {

	@MockBean
	private CompanyService companyService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser(roles = "ADMIN")
	@Description("index_GET /admin/company: 会社情報の一覧ページ（admin/company/index）を表示でき、modelにcompanyが設定されること")
	public void index_test_1() throws Exception {
		Company company = new Company();
		company.setName("株式会社テスト");
		when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

		mockMvc.perform(get("/admin/company"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/company/index"))
				.andExpect(model().attributeExists("company"));

		verify(companyService, times(1)).findFirstCompanyByOrderByIdDesc();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@Description("edit_GET /admin/company/edit: 会社情報の編集ページ（admin/company/edit）を表示でき、modelにcompanyEditFormが設定されること")
	public void edit_test_1() throws Exception {
		Company company = new Company();
		company.setName("株式会社テスト");
		company.setPostalCode("1010022");
		company.setAddress("東京都千代田区神田1-1-1");
		company.setRepresentative("山田太郎");
		company.setEstablishmentDate("2000-01-01");
		company.setCapital("1000万円");
		company.setBusiness("IT");
		company.setNumberOfEmployees("20");

		when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

		mockMvc.perform(get("/admin/company/edit"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/company/edit"))
				.andExpect(model().attributeExists("companyEditForm"));

		verify(companyService, times(1)).findFirstCompanyByOrderByIdDesc();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@Description("update_POST /admin/company/update: 入力エラーがある場合、編集画面に戻り（admin/company/edit）、更新処理(updateCompany)が呼ばれないこと")
	public void update_test_1() throws Exception {
		Company company = new Company();
		when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

		CompanyEditForm errorForm = new CompanyEditForm("", "", "", "", "", "", "", "");

		mockMvc.perform(post("/admin/company/update")
				.with(csrf())
				.param("name", "")
				.param("postalCode", "")
				.param("address", "")
				.param("representative", "")
				.param("establishmentDate", "")
				.param("capital", "")
				.param("business", "")
				.param("numberOfEmployees", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/company/edit"))
				.andExpect(model().attributeExists("companyEditForm"));

		verify(companyService, never()).updateCompany(errorForm, company);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@Description("update_POST /admin/company/update: 入力が正しい場合、会社情報を更新し、/admin/companyへリダイレクトし、successMessageが設定されること")
	public void update_test_2() throws Exception {
		Company company = new Company();
		when(companyService.findFirstCompanyByOrderByIdDesc()).thenReturn(company);

		CompanyEditForm editForm = new CompanyEditForm(
				"株式会社テスト",
				"1010022",
				"東京都千代田区神田1-1-1",
				"山田太郎",
				"2000-01-01",
				"1000万円",
				"IT",
				"20");

		mockMvc.perform(post("/admin/company/update")
				.with(csrf())
				.param("name", "株式会社テスト")
				.param("postalCode", "1010022")
				.param("address", "東京都千代田区神田1-1-1")
				.param("representative", "山田太郎")
				.param("establishmentDate", "2000-01-01")
				.param("capital", "1000万円")
				.param("business", "IT")
				.param("numberOfEmployees", "20"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/company"))
				.andExpect(flash().attribute("successMessage", "会社情報を編集しました。"));

		verify(companyService, times(1)).findFirstCompanyByOrderByIdDesc();
		verify(companyService, times(1)).updateCompany(editForm, company);
	}
}
