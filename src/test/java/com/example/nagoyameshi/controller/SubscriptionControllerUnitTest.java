package com.example.nagoyameshi.controller;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@WebMvcTest(SubscriptionController.class)
public class SubscriptionControllerUnitTest {

  private static final String PREMIUM_PRICE_ID = "price_1RnbHSDFrrvVAjizYFtY4r0N";

  @MockBean
  private UserService userService;

  @MockBean
  private StripeService stripeService;

  @Autowired
  private MockMvc mockMvc;

  private User freeUser;
  private User paidUser;
  private UserDetailsImpl freePrincipal;
  private UserDetailsImpl paidPrincipal;

  @BeforeEach
  void setUp() {
    Role freeRole = new Role();
    freeRole.setName("ROLE_FREE_MEMBER");
    freeUser = new User();
    freeUser.setEmail("free@example.com");
    freeUser.setRole(freeRole);
    freeUser.setEnabled(true);

    Role paidRole = new Role();
    paidRole.setName("ROLE_PAID_MEMBER");
    paidUser = new User();
    paidUser.setEmail("paid@example.com");
    paidUser.setRole(paidRole);
    paidUser.setEnabled(true);
    paidUser.setStripeCustomerId("cus_123");

    freePrincipal = new UserDetailsImpl(freeUser, List.of());
    paidPrincipal = new UserDetailsImpl(paidUser, List.of());
  }

  private StripeException stripeException() {
    return mock(StripeException.class);
  }

  @Test
  @Description("GET /subscription/register ビュー表示")
  public void register_test_1() throws Exception {
    mockMvc.perform(get("/subscription/register").with(user(freePrincipal)))
        .andExpect(status().isOk())
        .andExpect(view().name("subscription/register"));
  }

  @Test
  @Description("POST /subscription/create 初回登録で顧客作成し成功する")
  public void create_test_1() throws Exception {
    Customer customer = new Customer();
    customer.setId("cus_new");
    when(stripeService.createCustomer(freeUser)).thenReturn(customer);
    doAnswer(invocation -> {
      freeUser.setStripeCustomerId("cus_new");
      return null;
    }).when(userService).saveStripeCustomerId(freeUser, "cus_new");

    mockMvc.perform(post("/subscription/create")
        .with(csrf())
        .param("paymentMethodId", "pm_123")
        .with(user(freePrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "有料プランへの登録が完了しました。"));

    verify(stripeService).createCustomer(freeUser);
    verify(userService).saveStripeCustomerId(freeUser, "cus_new");
    verify(stripeService).attachPaymentMethodToCustomer("pm_123", "cus_new");
    verify(stripeService).setDefaultPaymentMethod("pm_123", "cus_new");
    verify(stripeService).createSubscription("cus_new", PREMIUM_PRICE_ID);
    verify(userService).updateRole(freeUser, "ROLE_PAID_MEMBER");
    verify(userService).refreshAuthenticationByRole("ROLE_PAID_MEMBER");
  }

  @Test
  @Description("POST /subscription/create 顧客作成で例外ならリダイレクトしエラーメッセージ")
  public void create_test_2() throws Exception {
    when(stripeService.createCustomer(freeUser)).thenThrow(stripeException());

    mockMvc.perform(post("/subscription/create")
        .with(csrf())
        .param("paymentMethodId", "pm_err")
        .with(user(freePrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。"));

    verify(stripeService).createCustomer(freeUser);
  }

  @Test
  @Description("POST /subscription/create 既存顧客で決済処理を行う")
  public void create_test_3() throws Exception {
    mockMvc.perform(post("/subscription/create")
        .with(csrf())
        .param("paymentMethodId", "pm_exist")
        .with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "有料プランへの登録が完了しました。"));

    verify(stripeService).attachPaymentMethodToCustomer("pm_exist", "cus_123");
    verify(stripeService).setDefaultPaymentMethod("pm_exist", "cus_123");
    verify(stripeService).createSubscription("cus_123", PREMIUM_PRICE_ID);
    verify(userService).updateRole(paidUser, "ROLE_PAID_MEMBER");
    verify(userService).refreshAuthenticationByRole("ROLE_PAID_MEMBER");
  }

  @Test
  @Description("POST /subscription/create 決済処理で例外ならエラーメッセージ")
  public void create_test_4() throws Exception {
    doThrow(stripeException())
        .when(stripeService).attachPaymentMethodToCustomer("pm_fail", "cus_123");

    mockMvc.perform(post("/subscription/create")
        .with(csrf())
        .param("paymentMethodId", "pm_fail")
        .with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。"));

    verify(stripeService).attachPaymentMethodToCustomer("pm_fail", "cus_123");
    verify(stripeService, never()).setDefaultPaymentMethod("pm_fail", "cus_123");
    verify(userService, never()).updateRole(paidUser, "ROLE_PAID_MEMBER");
  }

  @Test
  @Description("GET /subscription/edit 支払い方法を表示する")
  public void edit_test_1() throws Exception {
    PaymentMethod paymentMethod = mock(PaymentMethod.class);
    PaymentMethod.Card card = mock(PaymentMethod.Card.class);
    PaymentMethod.BillingDetails billingDetails = mock(PaymentMethod.BillingDetails.class);
    when(paymentMethod.getCard()).thenReturn(card);
    when(paymentMethod.getBillingDetails()).thenReturn(billingDetails);
    when(billingDetails.getName()).thenReturn("Taro Samurai");
    when(stripeService.getDefaultPaymentMethod("cus_123")).thenReturn(paymentMethod);

    mockMvc.perform(get("/subscription/edit").with(user(paidPrincipal)))
        .andExpect(status().isOk())
        .andExpect(view().name("subscription/edit"))
        .andExpect(model().attribute("card", card))
        .andExpect(model().attribute("cardHolderName", "Taro Samurai"));

    verify(stripeService).getDefaultPaymentMethod("cus_123");
  }

  @Test
  @Description("GET /subscription/edit 取得失敗でリダイレクト")
  public void edit_test_2() throws Exception {
    when(stripeService.getDefaultPaymentMethod("cus_123"))
        .thenThrow(stripeException());

    mockMvc.perform(get("/subscription/edit").with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。"));

    verify(stripeService).getDefaultPaymentMethod("cus_123");
  }

  @Test
  @Description("POST /subscription/update 支払い方法を更新する")
  public void update_test_1() throws Exception {
    when(stripeService.getDefaultPaymentMethodId("cus_123")).thenReturn("pm_old");

    mockMvc.perform(post("/subscription/update")
        .with(csrf())
        .param("paymentMethodId", "pm_new")
        .with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "お支払い方法を変更しました。"));

    verify(stripeService).getDefaultPaymentMethodId("cus_123");
    verify(stripeService).attachPaymentMethodToCustomer("pm_new", "cus_123");
    verify(stripeService).setDefaultPaymentMethod("pm_new", "cus_123");
    verify(stripeService).detachPaymentMethodFromCustomer("pm_old");
  }

  @Test
  @Description("POST /subscription/update 変更失敗でエラーメッセージ")
  public void update_test_2() throws Exception {
    when(stripeService.getDefaultPaymentMethodId("cus_123"))
        .thenThrow(stripeException());

    mockMvc.perform(post("/subscription/update")
        .with(csrf())
        .param("paymentMethodId", "pm_new")
        .with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。"));

    verify(stripeService).getDefaultPaymentMethodId("cus_123");
    verify(stripeService, never()).attachPaymentMethodToCustomer("pm_new", "cus_123");
  }

  @Test
  @Description("GET /subscription/cancel ビュー表示")
  public void cancel_test_1() throws Exception {
    mockMvc.perform(get("/subscription/cancel").with(user(paidPrincipal)))
        .andExpect(status().isOk())
        .andExpect(view().name("subscription/cancel"));
  }

  @Test
  @Description("POST /subscription/delete サブスク解約を実行する")
  public void delete_test_1() throws Exception {
    Subscription subscription = new Subscription();
    when(stripeService.getSubscriptions("cus_123")).thenReturn(List.of(subscription));
    when(stripeService.getDefaultPaymentMethodId("cus_123")).thenReturn("pm_default");

    mockMvc.perform(post("/subscription/delete").with(csrf()).with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "有料プランを解約しました。"));

    verify(stripeService).getSubscriptions("cus_123");
    verify(stripeService).cancelSubscriptions(List.of(subscription));
    verify(stripeService).getDefaultPaymentMethodId("cus_123");
    verify(stripeService).detachPaymentMethodFromCustomer("pm_default");
    verify(userService).updateRole(paidUser, "ROLE_FREE_MEMBER");
    verify(userService).refreshAuthenticationByRole("ROLE_FREE_MEMBER");
  }

  @Test
  @Description("POST /subscription/delete 解約処理失敗でエラーメッセージ")
  public void delete_test_2() throws Exception {
    when(stripeService.getSubscriptions("cus_123"))
        .thenThrow(stripeException());

    mockMvc.perform(post("/subscription/delete").with(csrf()).with(user(paidPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。"));

    verify(stripeService).getSubscriptions("cus_123");
    verify(stripeService, never()).cancelSubscriptions(List.of());
    verify(userService, never()).updateRole(paidUser, "ROLE_FREE_MEMBER");
  }
}
