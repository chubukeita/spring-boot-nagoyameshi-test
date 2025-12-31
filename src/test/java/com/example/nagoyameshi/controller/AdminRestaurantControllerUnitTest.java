package com.example.nagoyameshi.controller;

import static org.mockito.ArgumentMatchers.any;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryRestaurantService;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RegularHolidayRestaurantService;
import com.example.nagoyameshi.service.RegularHolidayService;
import com.example.nagoyameshi.service.RestaurantNavService;
import com.example.nagoyameshi.service.RestaurantService;

@WebMvcTest(AdminRestaurantController.class)
public class AdminRestaurantControllerUnitTest {

  @MockBean
  private RestaurantService restaurantService;

  @MockBean
  private CategoryService categoryService;

  @MockBean
  private CategoryRestaurantService categoryRestaurantService;

  @MockBean
  private RegularHolidayService regularHolidayService;

  @MockBean
  private RegularHolidayRestaurantService regularHolidayRestaurantService;

  @MockBean
  private RestaurantNavService restaurantNavService;

  @Autowired
  private MockMvc mockMvc;

  private UserDetailsImpl adminUserDetails;
  private Restaurant restaurant;

  @BeforeEach
  public void setUp() {
    User adminUser = new User();
    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");
    adminUser.setRole(adminRole);
    adminUserDetails = new UserDetailsImpl(adminUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    restaurant = new Restaurant();
    restaurant.setId(1);
    restaurant.setName("テスト店舗");
  }

  @Test
  @Description("GET /admin/restaurants 管理者としてログイン済みの場合は店舗一覧ページが正しく表示される")
  public void index_test_1() throws Exception {
    Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
    Page<Restaurant> restaurantPage = new PageImpl<>(Collections.singletonList(restaurant), pageable, 1);

    when(restaurantService.findAllRestaurants(pageable)).thenReturn(restaurantPage);

    mockMvc.perform(get("/admin/restaurants").with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/index"))
        .andExpect(model().attribute("restaurantPage", restaurantPage));

    verify(restaurantService).findAllRestaurants(pageable);
  }

  @Test
  @Description("GET /admin/restaurants キーワード検索で店舗一覧ページが正しく表示される")
  public void index_test_2() throws Exception {
    Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.ASC, "id"));
    Page<Restaurant> restaurantPage = new PageImpl<>(Collections.singletonList(restaurant), pageable, 1);
    String testKeyword = "テスト";

    when(restaurantService.findRestaurantsByNameLike(testKeyword, pageable)).thenReturn(restaurantPage);

    mockMvc.perform(get("/admin/restaurants")
        .param("keyword", testKeyword)
        .with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/index"))
        .andExpect(model().attribute("restaurantPage", restaurantPage))
        .andExpect(model().attribute("keyword", testKeyword));

    verify(restaurantService).findRestaurantsByNameLike(testKeyword, pageable);
  }

  @Test
  @Description("GET /admin/restaurants/{id} 店舗が存在しない場合は店舗一覧ページにリダイレクトされる")
  public void show_test_1() throws Exception {
    Integer restaurantId = 999;

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

    mockMvc.perform(get("/admin/restaurants/999").with(user(adminUserDetails)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("errorMessage", "店舗が存在しません。"));

    verify(restaurantService).findRestaurantById(restaurantId);
  }

  @Test
  @Description("POST /admin/restaurants/{id}/delete 店舗が正常に削除された場合は店舗一覧ページにリダイレクトされる")
  public void delete_test_1() throws Exception {
    Integer restaurantId = 1;

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

    mockMvc.perform(post("/admin/restaurants/1/delete")
        .with(user(adminUserDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("successMessage", "店舗を削除しました。"));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(restaurantService).deleteRestaurant(restaurant);
  }

  @Test
  @Description("POST /admin/restaurants/{id}/delete 店舗が存在しない場合は店舗一覧ページにリダイレクトされる")
  public void delete_test_2() throws Exception {
    Integer restaurantId = 999;

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

    mockMvc.perform(post("/admin/restaurants/999/delete")
        .with(user(adminUserDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("errorMessage", "店舗が存在しません。"));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(restaurantService, never()).deleteRestaurant(restaurant);
  }

  @Test
  @Description("GET /admin/restaurants/register 管理者としてログイン済みの場合は店舗登録ページが正しく表示される")
  public void register_test_1() throws Exception {
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();

    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(get("/admin/restaurants/register").with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/register"))
        .andExpect(model().attributeExists("restaurantRegisterForm"))
        .andExpect(model().attribute("categories", categories))
        .andExpect(model().attribute("regularHolidays", regularHolidays));

    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
  }

  @Test
  @Description("GET /admin/restaurants/{id}/edit 店舗が存在しない場合は店舗一覧ページにリダイレクトされる")
  public void edit_test_1() throws Exception {
    Integer restaurantId = 999;

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

    mockMvc.perform(get("/admin/restaurants/999/edit").with(user(adminUserDetails)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("errorMessage", "店舗が存在しません。"));

    verify(restaurantService).findRestaurantById(restaurantId);
  }

  @Test
  @Description("POST /admin/restaurants/{id}/update 店舗が存在しない場合は店舗一覧ページにリダイレクトされる")
  public void update_test_1() throws Exception {
    Integer restaurantId = 999;

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

    mockMvc.perform(post("/admin/restaurants/999/update")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "更新店舗")
        .param("description", "説明")
        .param("lowestPrice", "1000")
        .param("highestPrice", "3000")
        .param("postalCode", "1010001")
        .param("address", "東京都千代田区")
        .param("openingTime", "10:00")
        .param("closingTime", "22:00")
        .param("seatingCapacity", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("errorMessage", "店舗が存在しません。"));

    verify(restaurantService).findRestaurantById(restaurantId);
  }

  @Test
  @Description("GET /admin/restaurants/{id}/edit 店舗が存在する場合は店舗編集ページが正しく表示される")
  public void edit_test_2() throws Exception {
    Integer restaurantId = 1;
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();
    List<Integer> categoryIds = Collections.emptyList();
    List<Integer> regularHolidayIds = Collections.emptyList();

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant)).thenReturn(categoryIds);
    when(regularHolidayRestaurantService.findRegularHolidayIdsByRestaurant(restaurant)).thenReturn(regularHolidayIds);
    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(get("/admin/restaurants/1/edit").with(user(adminUserDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/edit"))
        .andExpect(model().attribute("restaurant", restaurant))
        .andExpect(model().attributeExists("restaurantEditForm"))
        .andExpect(model().attribute("categories", categories))
        .andExpect(model().attribute("regularHolidays", regularHolidays));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(categoryRestaurantService).findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
    verify(regularHolidayRestaurantService).findRegularHolidayIdsByRestaurant(restaurant);
    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
  }

  @Test
  @Description("POST /admin/restaurants/create バリデーションエラーがない場合は店舗が正常に作成される")
  public void create_successfullyCreates() throws Exception {
    com.example.nagoyameshi.form.RestaurantRegisterForm registerForm = new com.example.nagoyameshi.form.RestaurantRegisterForm();
    registerForm.setName("新規店舗");
    registerForm.setDescription("新規店舗の説明");
    registerForm.setLowestPrice(1000);
    registerForm.setHighestPrice(3000);
    registerForm.setPostalCode("1010001");
    registerForm.setAddress("東京都千代田区");
    registerForm.setOpeningTime(java.time.LocalTime.parse("10:00"));
    registerForm.setClosingTime(java.time.LocalTime.parse("22:00"));
    registerForm.setSeatingCapacity(50);

    when(restaurantService.isValidPrices(1000, 3000)).thenReturn(true);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("10:00"), java.time.LocalTime.parse("22:00")))
        .thenReturn(true);

    mockMvc.perform(post("/admin/restaurants/create")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "新規店舗")
        .param("description", "新規店舗の説明")
        .param("lowestPrice", "1000")
        .param("highestPrice", "3000")
        .param("postalCode", "1010001")
        .param("address", "東京都千代田区")
        .param("openingTime", "10:00")
        .param("closingTime", "22:00")
        .param("seatingCapacity", "50"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("successMessage", "店舗を登録しました。"));

    verify(restaurantService).isValidPrices(1000, 3000);
    verify(restaurantService).isValidBusinessHours(java.time.LocalTime.parse("10:00"),
        java.time.LocalTime.parse("22:00"));
    verify(restaurantService).createRestaurant(registerForm);
  }

  @Test
  @Description("POST /admin/restaurants/create 価格のバリデーションエラーがある場合は店舗登録ページに戻る")
  public void create_returnsToRegisterPageWhenPriceValidationFails() throws Exception {
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();

    when(restaurantService.isValidPrices(3000, 1000)).thenReturn(false);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("10:00"), java.time.LocalTime.parse("22:00")))
        .thenReturn(true);
    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(post("/admin/restaurants/create")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "新規店舗")
        .param("description", "新規店舗の説明")
        .param("lowestPrice", "3000")
        .param("highestPrice", "1000")
        .param("postalCode", "1010001")
        .param("address", "東京都千代田区")
        .param("openingTime", "10:00")
        .param("closingTime", "22:00")
        .param("seatingCapacity", "50"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/register"))
        .andExpect(model().attributeExists("restaurantRegisterForm"))
        .andExpect(model().hasErrors())
        .andExpect(model().attributeHasFieldErrors("restaurantRegisterForm", "lowestPrice", "highestPrice"));

    verify(restaurantService).isValidPrices(3000, 1000);
    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
    verify(restaurantService, never()).createRestaurant(any());
  }

  @Test
  @Description("POST /admin/restaurants/create 営業時間のバリデーションエラーがある場合は店舗登録ページに戻る")
  public void create_returnsToRegisterPageWhenBusinessHoursValidationFails() throws Exception {
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();

    when(restaurantService.isValidPrices(1000, 3000)).thenReturn(true);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("22:00"), java.time.LocalTime.parse("10:00")))
        .thenReturn(false);
    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(post("/admin/restaurants/create")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "新規店舗")
        .param("description", "新規店舗の説明")
        .param("lowestPrice", "1000")
        .param("highestPrice", "3000")
        .param("postalCode", "1010001")
        .param("address", "東京都千代田区")
        .param("openingTime", "22:00")
        .param("closingTime", "10:00")
        .param("seatingCapacity", "50"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/register"))
        .andExpect(model().attributeExists("restaurantRegisterForm"))
        .andExpect(model().hasErrors())
        .andExpect(model().attributeHasFieldErrors("restaurantRegisterForm", "openingTime", "closingTime"));

    verify(restaurantService).isValidBusinessHours(java.time.LocalTime.parse("22:00"),
        java.time.LocalTime.parse("10:00"));
    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
    verify(restaurantService, never()).createRestaurant(any());
  }

  @Test
  @Description("POST /admin/restaurants/{id}/update バリデーションエラーがない場合は店舗が正常に更新される")
  public void update_successfullyUpdates() throws Exception {
    Integer restaurantId = 1;
    com.example.nagoyameshi.form.RestaurantEditForm editForm = new com.example.nagoyameshi.form.RestaurantEditForm();
    editForm.setName("更新店舗");
    editForm.setDescription("更新後の説明");
    editForm.setLowestPrice(1500);
    editForm.setHighestPrice(3500);
    editForm.setPostalCode("1010002");
    editForm.setAddress("東京都千代田区更新");
    editForm.setOpeningTime(java.time.LocalTime.parse("11:00"));
    editForm.setClosingTime(java.time.LocalTime.parse("23:00"));
    editForm.setSeatingCapacity(60);

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(restaurantService.isValidPrices(1500, 3500)).thenReturn(true);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("11:00"), java.time.LocalTime.parse("23:00")))
        .thenReturn(true);

    mockMvc.perform(post("/admin/restaurants/1/update")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "更新店舗")
        .param("description", "更新後の説明")
        .param("lowestPrice", "1500")
        .param("highestPrice", "3500")
        .param("postalCode", "1010002")
        .param("address", "東京都千代田区更新")
        .param("openingTime", "11:00")
        .param("closingTime", "23:00")
        .param("seatingCapacity", "60"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/restaurants"))
        .andExpect(flash().attribute("successMessage", "店舗を編集しました。"));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(restaurantService).isValidPrices(1500, 3500);
    verify(restaurantService).isValidBusinessHours(java.time.LocalTime.parse("11:00"),
        java.time.LocalTime.parse("23:00"));
    verify(restaurantService).updateRestaurant(editForm, restaurant);
  }

  @Test
  @Description("POST /admin/restaurants/{id}/update 価格のバリデーションエラーがある場合は店舗編集ページに戻る")
  public void update_returnsToEditPageWhenPriceValidationFails() throws Exception {
    Integer restaurantId = 1;
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(restaurantService.isValidPrices(3500, 1500)).thenReturn(false);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("11:00"), java.time.LocalTime.parse("23:00")))
        .thenReturn(true);
    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(post("/admin/restaurants/1/update")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "更新店舗")
        .param("description", "更新後の説明")
        .param("lowestPrice", "3500")
        .param("highestPrice", "1500")
        .param("postalCode", "1010002")
        .param("address", "東京都千代田区更新")
        .param("openingTime", "11:00")
        .param("closingTime", "23:00")
        .param("seatingCapacity", "60"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/edit"))
        .andExpect(model().attribute("restaurant", restaurant))
        .andExpect(model().attributeExists("restaurantEditForm"))
        .andExpect(model().hasErrors())
        .andExpect(model().attributeHasFieldErrors("restaurantEditForm", "lowestPrice", "highestPrice"));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(restaurantService).isValidPrices(3500, 1500);
    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
    verify(restaurantService, never()).updateRestaurant(any(), any());
  }

  @Test
  @Description("POST /admin/restaurants/{id}/update 営業時間のバリデーションエラーがある場合は店舗編集ページに戻る")
  public void update_returnsToEditPageWhenBusinessHoursValidationFails() throws Exception {
    Integer restaurantId = 1;
    List<com.example.nagoyameshi.entity.Category> categories = Collections.emptyList();
    List<com.example.nagoyameshi.entity.RegularHoliday> regularHolidays = Collections.emptyList();

    when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(restaurantService.isValidPrices(1500, 3500)).thenReturn(true);
    when(restaurantService.isValidBusinessHours(java.time.LocalTime.parse("23:00"), java.time.LocalTime.parse("11:00")))
        .thenReturn(false);
    when(categoryService.findAllCategories()).thenReturn(categories);
    when(regularHolidayService.findAllRegularHolidays()).thenReturn(regularHolidays);

    mockMvc.perform(post("/admin/restaurants/1/update")
        .with(user(adminUserDetails))
        .with(csrf())
        .param("name", "更新店舗")
        .param("description", "更新後の説明")
        .param("lowestPrice", "1500")
        .param("highestPrice", "3500")
        .param("postalCode", "1010002")
        .param("address", "東京都千代田区更新")
        .param("openingTime", "23:00")
        .param("closingTime", "11:00")
        .param("seatingCapacity", "60"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/restaurants/edit"))
        .andExpect(model().attribute("restaurant", restaurant))
        .andExpect(model().attributeExists("restaurantEditForm"))
        .andExpect(model().hasErrors())
        .andExpect(model().attributeHasFieldErrors("restaurantEditForm", "openingTime", "closingTime"));

    verify(restaurantService).findRestaurantById(restaurantId);
    verify(restaurantService).isValidBusinessHours(java.time.LocalTime.parse("23:00"),
        java.time.LocalTime.parse("11:00"));
    verify(categoryService).findAllCategories();
    verify(regularHolidayService).findAllRegularHolidays();
    verify(restaurantService, never()).updateRestaurant(any(), any());
  }
}
