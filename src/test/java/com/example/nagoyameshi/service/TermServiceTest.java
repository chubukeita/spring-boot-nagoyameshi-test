package com.example.nagoyameshi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.repository.TermRepository;

@ExtendWith(MockitoExtension.class)
public class TermServiceTest {

  @Mock
  private TermRepository termRepository;

  @InjectMocks
  private TermService termService;

  @Test
  @Description("findFirstTermByOrderByIdDesc_最新を返すこと")
  void findFirstTermByOrderByIdDesc_test_1() {
    Term term = new Term();
    term.setId(2);
    when(termRepository.findFirstByOrderByIdDesc()).thenReturn(term);

    Term result = termService.findFirstTermByOrderByIdDesc();

    assertEquals(2, result.getId());
    verify(termRepository).findFirstByOrderByIdDesc();
  }

  @Test
  @Description("updateTerm_内容を上書きして保存すること")
  void updateTerm_test_1() {
    Term term = new Term();
    term.setContent("old");
    TermEditForm form = new TermEditForm("new content");

    termService.updateTerm(form, term);

    assertEquals("new content", term.getContent());
    verify(termRepository).save(term);
  }
}
