# nagoyameshi テスト実装ドキュメント

**作成日**: 2025年12月31日  
**対象プロジェクト**: nagoyameshi (Spring Boot)  
**ステータス**: ✅ テスト実装完了

### 実装完了状況

JUnit5による包括的なテストスイートが実装されています。以下の構成となっています：

| カテゴリ | テスト数 |
|--------|---------|
| **Controller Unit Tests** | 106個 |
| **Service Tests** | 126個 |
| **Repository Tests** | 58個 |
| **合計** | **290個** |


---

## 🚀 クイックスタート

### テスト実行

```bash
# 全テスト実行
mvn clean test

# 特定のテストクラスのみ実行
mvn clean test -Dtest=UserRepositoryTest

# 特定のテストメソッドのみ実行
mvn clean test -Dtest=UserRepositoryTest#findByEmail_test1
```

### テスト結果確認

```bash
# テスト完了後、以下のファイルで結果を確認できます
target/surefire-reports/
```

---


## 全体統計

```
テストカテゴリ別:
- Controller Unit Tests: 106個
- Service Tests: 126個
- Repository Tests: 58個 
```

### カテゴリ別カバレッジ

| カテゴリ | テスト数 | 網羅状況 |
|--------|--------|--------|
| **ServiceTest** | 126個 |
| **ControllerUnitTest** | 106個 |
| **RepositoryTest** | 58個 |


**Service層**:
- CategoryServiceTest, CompanyServiceTest, FavoriteServiceTest
- UserServiceTest, ReservationServiceTest, RestaurantServiceTest
- ReviewServiceTest, RejoinServiceTest, ResetServiceTest
- RegularHolidayServiceTest, RegularHolidayRestaurantServiceTest
- RejoinTokenServiceTest, ResetTokenServiceTest
- TermServiceTest, VerificationTokenServiceTest
- CategoryRestaurantServiceTest, UserNavServiceTest
- RestaurantNavServiceTest, StripeServiceTest

**Controller層**:
- AdminHomeControllerUnitTest, AdminUserControllerUnitTest
- AdminCompanyControllerUnitTest, AdminTermControllerUnitTest
- AdminRestaurantControllerUnitTest, AdminCategoryControllerUnitTest
- HomeControllerUnitTest, CompanyControllerUnitTest
- WithdrawalControllerTest, TermControllerUnitTest
- SubscriptionControllerUnitTest, ReviewControllerUnitTest
- ResetControllerUnitTest, RejoinControllerUnitTest
- FavoriteControllerUnitTest, RestaurantControllerUnitTest
- ReservationControllerUnitTest, UserControllerUnitTest

**Repository層**:
- CategoryRepositoryTest, CompanyRepositoryTest
- FavoriteRepositoryTest, RegularHolidayRepositoryTest
- RegularHolidayRestaurantRepositoryTest, ReservationRepositoryTest
- ResetTokenRepositoryTest, ReviewRepositoryTest
- RoleRepositoryTest, TermRepositoryTest
- VerificationTokenRepositoryTest, CategoryRestaurantRepositoryTest
- RejoinTokenRepositoyTest, RestaurantRepositoryTest
- UserRepositoryTest

---

## 📚 テストの構成

### 1. Controller Unit Tests（106個）

**目的**: エンドポイント、バリデーション、認証・認可、リダイレクト処理の検証

**テストクラス** (17個):
- AdminHomeControllerUnitTest
- AdminCategoryControllerUnitTest
- AdminCompanyControllerUnitTest
- AdminRestaurantControllerUnitTest
- AdminUserControllerUnitTest
- AdminTermControllerUnitTest
- CompanyControllerUnitTest
- FavoriteControllerUnitTest
- HomeControllerUnitTest
- RejoinControllerUnitTest
- ReservationControllerUnitTest
- ResetControllerUnitTest
- RestaurantControllerUnitTest
- ReviewControllerUnitTest
- SubscriptionControllerUnitTest
- TermControllerUnitTest
- UserControllerUnitTest
- WithdrawalControllerUnitTest

**検証項目**:
- ✅ HTTPステータスコード
- ✅ ビューの遷移
- ✅ Modelへの属性設定
- ✅ バリデーションエラー処理
- ✅ 認証・認可（@WithMockUser, @AuthenticationPrincipal）
- ✅ CSRFトークン検証
- ✅ リダイレクト処理

**実装手法**:
- `@WebMvcTest`: MockMvcを使用したコントローラ単体テスト
- Mockito: 依存するServiceのモック化
- Spring Security Test: 認証・認可テスト

---

### 2. Service Tests（126個）

**目的**: ビジネスロジック、エラーハンドリング、データ処理の検証

**主なテストクラス** (19個):
- CategoryRestaurantServiceTest
- CategoryServiceTest
- CompanyServiceTest
- FavoriteServiceTest
- RegularHolidayRestaurantServiceTest
- RegularHolidayServiceTest
- RejoinServiceTest
- RejoinTokenServiceTest
- ReservationServiceTest
- ResetServiceTest
- ResetTokenServiceTest
- RestaurantNavServiceTest
- RestaurantServiceTest
- ReviewServiceTest
- StripeServiceTest (11個テスト)
- TermServiceTest
- UserNavServiceTest
- UserServiceTest
- VerificationTokenServiceTest

**Stripe Service テスト** (11個):
- `stripeService_test_1`: インスタンス化確認
- `createCustomer_test_1`: 顧客作成
- `attachPaymentMethodToCustomer_test_1`: 支払い方法紐付け
- `setDefaultPaymentMethod_test_1`: デフォルト支払い方法設定
- `createSubscription_test_1`: サブスクリプション作成
- `getDefaultPaymentMethod_test_1`: デフォルト支払い方法取得
- `getDefaultPaymentMethodId_test_1`: デフォルト支払い方法ID取得
- `detachPaymentMethodFromCustomer_test_1`: 支払い方法解除
- `getSubscriptions_test_1`: サブスクリプション一覧取得
- `cancelSubscriptions_test_1`: サブスクリプション取消
- その他のテスト

**検証項目**:
- ✅ 正常系処理
- ✅ 異常系処理（例外ハンドリング）
- ✅ Repositoryメソッド呼び出しの確認
- ✅ データ変換・加工ロジック
- ✅ バリデーション

**実装手法**:
- `@ExtendWith(MockitoExtension.class)`: Mockito設定
- `@Mock/@InjectMocks`: 依存性の注入とモック化
- Mockito: メソッド呼び出しの検証（verify）
- AssertJ: アサーション

---

### 3. Repository Tests（58個）

**目的**: データベース操作、クエリ実行、データ永続化の検証

**テストクラス** (15個):
- CategoryRepositoryTest (4個)
- CategoryRestaurantRepositoryTest (3個)
- CompanyRepositoryTest (1個)
- FavoriteRepositoryTest (2個)
- RegularHolidayRepositoryTest (1個)
- RegularHolidayRestaurantRepositoryTest (3個)
- RejoinTokenRepositoryTest (2個)
- ReservationRepositoryTest (2個)
- ResetTokenRepositoryTest (2個)
- RestaurantRepositoryTest (3個)
- ReviewRepositoryTest (3個)
- RoleRepositoryTest (2個)
- TermRepositoryTest (1個)
- UserRepositoryTest (24個 - 最大)
- VerificationTokenRepositoryTest (1個)

**UserRepositoryTest の詳細** (24個テスト):

カスタムメソッド検証:
- `findByEmail`: メール検索（複数ユーザー対応）
- `findByNameLikeOrFuriganaLike`: 名前・ふりがな検索（ページング対応）
- `countByRole_Name`: ロール別人数カウント
- `findByEmailAndDeletedAtIsNull`: 会員メール検索（削除フィルタ）
- `findAllActive`: 全会員取得（ページング）
- `findActiveByNameLikeOrFuriganaLike`: 会員名検索（ページング）
- `findAllIncludeDeleted`: 全ユーザー取得（削除含む、ソート順序付き）

JpaRepositoryデフォルトメソッド検証:
- `findById`: IDで検索
- `findAll`: 全件取得
- `save`: 保存
- `delete`: 削除

**検証項目**:
- ✅ カスタムクエリの正確性
- ✅ ページング処理（size, page, sort）
- ✅ ソート順序（ASC/DESC）
- ✅ フィルタリング（論理削除対応）
- ✅ null ハンドリング
- ✅ 複数件マッチ時の動作

**実装手法**:
- `@DataJpaTest`: JPA/H2データベステスト
- テストデータのセットアップ（@BeforeEach）
- AssertJ: アサーション

---

## 🔍 テスト実装のパターン

### Controller Unit Test の標準パターン

```java
@WebMvcTest(UserController.class)
public class UserControllerUnitTest {
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "USER")
    @Description("GET /user: ユーザー情報ページを表示できること")
    public void index_test_1() throws Exception {
        User user = new User();
        // ... テスト実装
        
        mockMvc.perform(get("/user")
            .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/index"))
            .andExpect(model().attributeExists("user"));
        
        verify(userService).findUserById(userId);
    }
}
```

**ポイント**:
- `@WebMvcTest`: コントローラのみをテスト
- `@WithMockUser`: 認証ユーザーをシミュレート
- `@Description`: テスト内容を明確に記述
- `verify()`: Serviceメソッド呼び出しを検証

---

### Service Test の標準パターン

```java
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @Description("createUser: 新規ユーザーを作成できること")
    public void createUser_test_1() {
        // Arrange
        UserRegisterForm form = new UserRegisterForm();
        form.setEmail("test@example.com");
        
        // Act
        userService.createUser(form);
        
        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

**ポイント**:
- `@Mock`: 依存するRepositoryをモック化
- `@InjectMocks`: Serviceに自動注入
- `verify()`: メソッド呼び出しを検証
- `any()`: 任意の引数マッチング

---

### Repository Test の標準パターン

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false"
})
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @BeforeEach
    public void setUp() {
        // テストデータ準備
        User taro = new User();
        taro.setEmail("taro@example.com");
        entityManager.persistAndFlush(taro);
    }
    
    @Test
    @Description("findByEmail: メールアドレスでユーザーを検索できること")
    public void findByEmail_test1() {
        User actual = userRepository.findByEmail("taro@example.com");
        
        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo("taro@example.com");
    }
}
```

**ポイント**:
- `@DataJpaTest`: JPA関連のテストのみ
- `TestEntityManager`: H2データベースにデータ挿入
- `@BeforeEach`: 各テスト前にテストデータ準備
- AssertJ: 直感的で読みやすいアサーション

---

## ✅ 実装チェックリスト
### テスト実行確認

```bash
# 実行コマンド
mvn clean test

# 期待結果
[INFO] BUILD SUCCESS
[INFO] Tests run: 449, Failures: 0, Errors: 0, Skipped: 0
```

### 外部参考資料

- [JUnit 5 公式ドキュメント](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito ドキュメント](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Test ドキュメント](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html)
- [AssertJ ドキュメント](https://assertj.github.io/assertj-core-features-highlight.html)

---

## 📝 テスト実装時の推奨事項

### 1. テストメソッド命名規則

```
[メソッド名]_[テストシナリオ]_test[番号]

例:
- findByEmail_test_1 (正常系)
- findByEmail_test_2 (異常系)
- createUser_test_1 (正常系)
- createUser_test_2 (例外発生)
```

### 2. @Description の記述

```java
@Description("findByEmail: メールアドレスでユーザーを正確に検索できること")
public void findByEmail_test_1() { ... }
```

### 3. テストデータの管理

- Repository Test: `@BeforeEach`でセットアップ
- Service Test: Fixture メソッドで準備
- Controller Test: Mockで返却値を指定

### 4. アサーション

```java
// ❌ 非推奨: assertEquals
assertEquals(expected, actual);

// ✅ 推奨: AssertJ
assertThat(actual)
    .isNotNull()
    .isEqualTo(expected)
    .hasFieldOrPropertyWithValue("name", "Taro");
```

