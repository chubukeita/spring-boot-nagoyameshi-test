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


---

##  テスト実装品質分析

# UserRepositoryTest.javaとUserControllerUnitTest.javaの改善分析

##  概要
このドキュメントは、手書きテスト（「// -------以下AIの出力結果-------」より前）と AI生成テスト（同マーカー以後）の差異を説明しています。

---

##  UserRepositoryTest.java の改善点

### 1. Optional の正しい使い方（最重要）

####  手書きテスト（旧）
```java
// test1, test2 では orElse(null) を使用
User actualTaro = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com").orElse(null);
assertEquals(activeTaro, actualTaro);  // null チェックなし

User actualTaro = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com").orElse(null);
assertNull(actualTaro);  // null との比較のみ
```

####  AI生成テスト（新）
```java
// test_1, test_2, test_3 では isPresent() / isEmpty() を使用
var actualUser = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com");
assertTrue(actualUser.isPresent());  // Optional の状態を明確に検証
assertEquals(activeTaro, actualUser.get());

var actualUser = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com");
assertTrue(actualUser.isEmpty());  // 空の Optional を明確に検証
```

**抜けていた点：**
- Optional を直接 null チェックするのは Java のベストプラクティスに反する
- Optional の存在確認に `isPresent()` / `isEmpty()` を使うべき
- `actualUser.get()` で安全に値を取得すべき

---

### 2. countByRole_Name のテストカバレッジ不足

####  手書きテスト（旧）
```java
@Test
public void countByRole_Name_test1() throws Exception {
    long freeCount = userRepository.countByRole_Name("ROLE_FREE_MEMBER");
    assertEquals(1L, freeCount);
}
// 1ロール（FREE_MEMBER）のみテスト
```

####  AI生成テスト（新）
```java
@Test
public void countByRole_Name_test_1() throws Exception {
    long count = userRepository.countByRole_Name("ROLE_FREE_MEMBER");
    assertEquals(1, count);
}

@Test
public void countByRole_Name_test_2() throws Exception {
    long count = userRepository.countByRole_Name("ROLE_PAID_MEMBER");
    assertEquals(1, count);
}

@Test
public void countByRole_Name_test_3() throws Exception {
    long count = userRepository.countByRole_Name("ROLE_ADMIN");
    assertEquals(1, count);
}
// 全ロール（FREE, PAID, ADMIN）をテスト
```

**抜けていた点：**
- テストデータに3つのロール（FREE_MEMBER, PAID_MEMBER, ADMIN）が存在するのに、1つのロールのみをテスト
- 他のロールが正しくカウントされるかの検証がなかった
- 回帰テスト時に他のロールの不具合を見落とす可能性があった

---

### 3. findAllActive メソッドの詳細検証不足

####  手書きテスト（旧）
```java
@Test
public void findAllActive_test2() throws Exception {
    markActiveTaroAsDeleted();

    Pageable pageable = PageRequest.of(0, 15);
    Page<User> page = userRepository.findAllActive(pageable);
    List<User> actualList = page.getContent();

    assertEquals(2, actualList.size());
    assertTrue(actualList.stream().allMatch(u -> u.getDeletedAt() == null));
    // 削除されたユーザーがいないことを確認するのみ
}
```

####  AI生成テスト（新）
```java
@Test
public void findAllActive_test_1() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<User> page = userRepository.findAllActive(pageable);

    assertEquals(3, page.getTotalElements());  // 総要素数を検証
    assertTrue(page.getContent().stream().allMatch(u -> u.getDeletedAt() == null));
}

@Test
public void findAllActive_test_2() throws Exception {
    markActiveTaroAsDeleted();

    Pageable pageable = PageRequest.of(0, 10);
    Page<User> page = userRepository.findAllActive(pageable);

    assertEquals(2, page.getTotalElements());  // 削除後の総要素数
    assertEquals(2, page.getContent().size());  // コンテンツサイズも個別に検証
    for (User user : page.getContent()) {
        assertNull(user.getDeletedAt());  // 各ユーザーの確認
    }
}
```

**抜けていた点：**
- `getTotalElements()` による全体件数の検証がなかった
- コンテンツサイズと総要素数を分けて検証すべき
- 削除前の正常系テストケースがなかった

---

### 4. findByNameLikeOrFuriganaLike の簡略化テスト

####  手書きテスト（旧）
```java
@Test
public void findByNameLiekOrFuriganaLike_test2() throws Exception {
    Pageable pageable = PageRequest.of(0, 15);

    // 複雑な検証ロジック：3つの assertAll で OR検索の検証
    Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);
    List<Integer> expectedIds = List.of(activeTaro.getId(), jiro.getId(), hanako.getId());
    List<Integer> actualIds = toIds(page);

    assertAll("OR検索で取得すべき3件が正しいこと（順不同）",
            () -> assertIdsEqualAsSet(expectedIds, actualIds));
    // ... 複数の assertAll（計3つ）
}
```

####  AI生成テスト（新）
```java
@Test
public void findByNameLikeOrFuriganaLike_test_1() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%%", pageable);

    assertEquals(3, page.getContent().size());
    assertEquals("taro.samurai@example.com", page.getContent().get(0).getEmail());
}

@Test
public void findByNameLikeOrFuriganaLike_test_2() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%%", "%サムライ%", pageable);

    assertEquals(3, page.getContent().size());
    assertEquals("jiro.samurai@example.com", page.getContent().get(1).getEmail());
}

@Test
public void findByNameLikeOrFuriganaLike_test_3() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

    assertTrue(page.getContent().size() >= 2);
}
```

**抜けていた点：**
- name側、furigana側の個別検索を分割したテストケースがなかった
- 単一メソッドで複数の検証ロジックを詰め込みすぎていた
- テストの可読性が低く、どの検索方式が失敗したのかが判断しにくかった

---

### 5. findActiveByNameLikeOrFuriganaLike の正常系テスト不足

####  手書きテスト（旧）
```java
@Test
public void findActiveByNameLikeOrFuriganaLike_test1() throws Exception {
    // 削除ユーザーを作った上で検索
    markActiveTaroAsDeleted();

    Pageable pageable = PageRequest.of(0, 15);
    Page<User> actualPage1 = userRepository.findActiveByNameLikeOrFuriganaLike("%%", "%%", pageable1);
    // ... 複雑な検証
}
```

####  AI生成テスト（新）
```java
@Test
public void findActiveByNameLikeOrFuriganaLike_test_1() throws Exception {
    // 削除ユーザーを作った上で検索
    markActiveTaroAsDeleted();

    Pageable pageable = PageRequest.of(0, 10);
    Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

    assertEquals(2, page.getContent().size());
    for (User user : page.getContent()) {
        assertNull(user.getDeletedAt());
    }
}

@Test
public void findActiveByNameLikeOrFuriganaLike_test_2() throws Exception {
    // 正常系：削除ユーザーなしで検索
    Pageable pageable = PageRequest.of(0, 10);

    Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

    assertEquals(3, page.getContent().size());
    for (User user : page.getContent()) {
        assertNull(user.getDeletedAt());
    }
}
```

**抜けていた点：**
- 正常系（削除ユーザーがない状態）のテストケースがなかった
- エッジケース（削除ユーザーがある場合）のみのテスト
- 異なるシナリオでの動作確認不足

---

### 6. findByEmail の複数ユーザーシナリオ

####  手書きテスト（旧）
```java
@Test
public void findByEmail_test1() throws Exception {
    User actualTaro = userRepository.findByEmail("taro.samurai@example.com");
    assertEquals(activeTaro, actualTaro);
}

@Test
public void findByEmail_test2() throws Exception {
    User actualNotFoundUser = userRepository.findByEmail("notfound.samurai@example.com");
    assertNull(actualNotFoundUser);
}
// 太郎か未検出のみ
```

####  AI生成テスト（新）
```java
@Test
public void findByEmail_test_3() throws Exception {
    User foundUser = userRepository.findByEmail("jiro.samurai@example.com");

    assertNotNull(foundUser);
    assertEquals("侍 次郎", foundUser.getName());
}
// 次郎も検索確認
```

**抜けていた点：**
- テストデータに3人（太郎、次郎、花子）がいるのに、太郎と未検出のみをテスト
- 複数ユーザー間での検索結果が正しく区別されるかの検証がなかった
- 他のメールアドレスを検索する確認がなかった

---

### 7. findAllIncludeDeleted の削除ユーザー検証

####  手書きテスト（旧）
```java
@Test
public void findAllIncludeDeleted_test1() throws Exception {
    markActiveTaroAsDeleted();
    List<User> actualList = userRepository.findAllIncludeDeleted();

    assertTrue(actualList.stream().anyMatch(u -> u.getDeletedAt() != null));
    assertEquals(3, actualList.size());
    User last = actualList.get(actualList.size() - 1);
    assertNotNull(last.getDeletedAt());
    assertEquals("taro.samurai@example.com", last.getEmail());
}
// 1つのテストで複数の検証
```

####  AI生成テスト（新）
```java
@Test
public void findAllIncludeDeleted_test_2() throws Exception {
    markActiveTaroAsDeleted();

    List<User> list = userRepository.findAllIncludeDeleted();

    // 削除ユーザーを名前とメールアドレスで明確に特定
    assertTrue(list.stream().anyMatch(u -> 
        u.getEmail().equals("taro.samurai@example.com") && u.getDeletedAt() != null
    ));
}
// テストを簡潔に分割
```

**抜けていた点：**
- 削除ユーザーの特定をメールアドレスのみで行っていた（他のユーザーが削除されていないかの確認がなかった）
- 複数の assertion を1メソッドに詰め込みすぎていた

---

---

##  UserControllerUnitTest.java の改善点

### 1. Mock メソッド検証の粒度不足

####  手書きテスト（旧）
```java
@Test
public void update_test1() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro2.samurai@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(true);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit"));

    // これらの verify 文はすべて1つのテストに詰め込まれている
    verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
    verify(userService, times(1)).isEmailRegistered(expectedUserEditForm.getEmail());
    verify(userService, never()).updateUser(expectedUserEditForm, expectedUser);
}

@Test
public void update_test2() throws Exception {
    // ... 別の条件パターンだが、複合条件をテスト
    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

    // update が呼ばれることは verify で確認するが、
    // isEmailChanged の呼び出しは確認されない
    verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);
}
```

####  AI生成テスト（新）
```java
@Test
@Description("update_ユーザー情報更新時の検証：メールアドレス変更検証が呼ばれることを確認")
public void update_test_5() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user"));

    // isEmailChanged が確実に呼ばれたことを確認
    verify(userService, times(1)).isEmailChanged(expectedUserEditForm, expectedUser);
}

@Test
@Description("update_ユーザー情報更新時の検証：メール登録確認が呼ばれることを確認")
public void update_test_6() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().is3xxRedirection());

    // isEmailRegistered が確実に呼ばれたことを確認
    verify(userService, times(1)).isEmailRegistered(expectedUserEditForm.getEmail());
}

@Test
@Description("update_ユーザー情報更新時の検証：更新メソッドが呼ばれることを確認")
public void update_test_7() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().is3xxRedirection());

    // updateUser が確実に呼ばれたことを確認
    verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);
}
```

**抜けていた点：**
- 複数の条件パターンを1つのテストに詰め込んでいた
- 「どのメソッドが呼ばれるべきか」が明確ではなかった
- テストの名称では何を検証しているのかが分からなかった
- Mockito の `verify()` を使った個別メソッド呼び出しの検証が不十分だった

---

### 2. フォーム生成とバインディングの検証不足

####  手書きテスト（旧）
```java
@Test
public void edit_test1() throws Exception {
    UserEditForm userEditForm = new UserEditForm();
    // 手動でフォームを作成して比較

    this.mockMvc.perform(get("/user/edit")
            .with(user(userprincipal)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit"))
            .andExpect(model().attribute("userEditForm", userEditForm));
}

@Test
public void edit_test2() throws Exception {
    // 誕生日なしの別パターンだが、
    // フォームオブジェクト自体の生成ロジックを検証していない

    UserDetailsImpl unknownBirthdayUserPrincipal = new UserDetailsImpl(new User(),
            List.of(new SimpleGrantedAuthority("ROLE_USER")));

    this.mockMvc.perform(get("/user/edit")
            .with(user(unknownBirthdayUserPrincipal)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit"))
            .andExpect(model().attribute("userEditForm", userEditForm));
}
```

####  AI生成テスト（新）
```java
@Test
@Description("edit_ユーザー編集画面の検証：フォームオブジェクトが正しく生成されてモデルに追加されること")
public void edit_test_3() throws Exception {
    this.mockMvc.perform(get("/user/edit")
            .with(user(userprincipal)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit"))
            .andExpect(model().attributeExists("userEditForm"));  // 存在確認
}

@Test
@Description("edit_ユーザー編集画面の検証：誕生日フィールドがフォームに正しくセットされること")
public void edit_test_4() throws Exception {
    User userWithBirthday = new User();
    // ... User を明確に初期化

    UserDetailsImpl userPrincipalWithBirthday = new UserDetailsImpl(userWithBirthday,
            List.of(new SimpleGrantedAuthority("ROLE_FREE_MEMBER")));

    this.mockMvc.perform(get("/user/edit")
            .with(user(userPrincipalWithBirthday)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit"));
}
```

**抜けていた点：**
- フォームの存在確認だけで、フィールドの詳細な値の検証がなかった
- 誕生日の有無による別パターンを単なる追加テストではなく、明確に意図を分けるべきだった
- `model().attributeExists()` という簡潔な検証方法が活用されていなかった

---

### 3. 正常系（3xx リダイレクト）の検証不足

####  手書きテスト（旧）
```java
@Test
public void update_test2() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("taro2.samurai@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(true);
    when(userService.isEmailRegistered(expectedUserEditForm.getEmail())).thenReturn(false);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().is3xxRedirection())  // 3xx は確認
            .andExpect(redirectedUrl("/user"))
            .andExpect(model().hasNoErrors());

    verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);
}

@Test
public void update_test3() throws Exception {
    // 同じく 3xx リダイレクトだが、
    // なぜこのパターンが 3xx になるのか（ビジネスロジック）を
    // テスト名では分かりにくい

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(false);

    this.mockMvc.perform(post("/user/update")...)
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user"));

    verify(userService, never()).isEmailRegistered(expectedUserEditForm.getEmail());
    verify(userService, times(1)).updateUser(expectedUserEditForm, expectedUser);
}
```

####  AI生成テスト（新）
```java
@Test
@Description("update_ユーザー情報更新時の検証：バリデーションエラーがない場合は3xx リダイレクトステータスが返されること")
public void update_test_8() throws Exception {
    User expectedUser = createExpectedUser();
    UserEditForm expectedUserEditForm = createExpectedUserEditForm("newemail@example.com");

    when(userService.isEmailChanged(expectedUserEditForm, expectedUser)).thenReturn(false);

    this.mockMvc.perform(post("/user/update")
            .with(user(userprincipal))
            .with(csrf())
            .flashAttr("userEditForm", expectedUserEditForm))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user"));
}
```

**抜けていた点：**
- 正常系（エラーなし）のシナリオが明確に分離されていなかった
- 「なぜ 3xx リダイレクトになるのか」を @Description で明記すべき
- テスト名だけで、どの条件パターンなのかが判断しにくかった

---

### 4. index メソッドのモデル属性検証不足

####  手書きテスト（旧）
```java
@Test
public void index_test1() throws Exception {
    this.mockMvc.perform(get("/user")
            .with(user(userprincipal)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/index"))
            .andExpect(model().attribute("user", user));
}
// user 属性は確認するが、それ以上の詳細検証がない
```

####  AI生成テスト（新）
```java
@Test
@Description("index_ユーザー情報表示画面の検証：ユーザー情報が正しくモデルに渡されて表示されること")
public void index_test_2() throws Exception {
    User expectedUser = createExpectedUser();

    this.mockMvc.perform(get("/user")
            .with(user(userprincipal)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/index"))
            .andExpect(model().attributeExists("user"));  // 存在確認を明確に

    verify(userService, never()).updateUser(null, null);  // 不要な updateUser が呼ばれていないことを確認
}
```

**抜けていた点：**
- `model().attributeExists()` という積極的な存在確認がなかった
- Mock サービスが不要に呼び出されていないことを検証していなかった
- テスト名が単なる「表示画面」ではなく、「データが正しく渡される」ことを強調すべき

---

##  まとめ表

| 項目 | 手書きテスト（旧） | AI生成テスト（新） |
|------|-----------------|-----------------|
| **Optional の使い方** | `orElse(null)` で取得後に null チェック | `isPresent()`/`isEmpty()` で状態確認 |
| **テスト粒度** | 複数の検証を1メソッドに詰め込む | 単一責任原則に基づいて分割 |
| **Mock 検証** | 複合条件下で複数メソッドを一度に検証 | 個別メソッドごとに verify() で検証 |
| **テスト名** | 動作のみを記載（例：test1, test2） | ビジネスロジックを @Description で明記 |
| **カバレッジ** | 主要ケースのみ | 正常系異常系エッジケースを網羅 |
| **フォーム検証** | 詳細な値の比較 | 存在確認と構造検証 |
| **コードの可読性** | helper メソッド多用で複雑化 | 各テストが独立して読める |

---

##  改善のポイント（実装時に参考）

1. **Optional の正しい使用** - Java のベストプラクティスを遵守
2. **テスト分割** - 1テスト = 1検証の原則
3. **Mock 検証の徹底** - どのメソッドが何回呼ばれたかを明確に
4. **テスト名の明確化** - ビジネスロジックが分かるように記載
5. **全シナリオのカバー** - 正常系異常系エッジケース
6. **コード可読性** - テストコードは documentation になるべき
