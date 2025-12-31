package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.service.TermService;

@WebMvcTest(TermController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TermControllerUnitTest {

  @MockBean
  private TermService termService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @Description("GET /terms: 最新の利用規約を取得しビューを返すこと")
  public void index_test_1() throws Exception {
    Term term = new Term();
    term.setId(5);
    term.setContent("最新の利用規約");
    when(termService.findFirstTermByOrderByIdDesc()).thenReturn(term);

    mockMvc.perform(get("/terms"))
        .andExpect(status().isOk())
        .andExpect(view().name("terms/index"))
        .andExpect(model().attributeExists("term"));

    verify(termService).findFirstTermByOrderByIdDesc();
  }
}
