package com.example.nagoyameshi.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = {
//		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;NON_KEYWORDS=DAY;DB_CLOSE_DELAY=-1",
//		"spring.datasource.driverClassName=org.h2.Driver",
//		"spring.datasource.username=sa",
//		"spring.datasource.password=",
//		"spring.sql.init.mode=never",
//		"spring.jpa.hibernate.ddl-auto=create-drop"
//})
@ActiveProfiles("test")
public class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	// role
	private Role freeMemberRole;
	private Role paidMemberRole;
	private Role adminRole;

	// users
	private User activeTaro;
	private User jiro;
	private User hanako;

	// -------------- テストコードの中で使うメソッド --------------

	// 定数PASSWORDには、Taro、Jiro、Hanakoともに全て共通の「password」をエンコード化した値を入れる
	private static final String PASSWORD = "$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7C0";

	// Userのサンプルを作るメソッド
	private User createUser(
			String name,
			String furigana,
			String email,
			LocalDate birthday,
			String occupation,
			Role role,
			boolean enabled,
			LocalDateTime deletedAt) {
		User user = new User();
		user.setName(name);
		user.setFurigana(furigana);
		user.setPostalCode("1010022");
		user.setAddress("東京都千代田区神田練塀町300番地");
		user.setPhoneNumber("09012345678");
		user.setBirthday(birthday);
		user.setOccupation(occupation);
		user.setEmail(email);
		user.setPassword(PASSWORD);
		user.setRole(role);
		user.setEnabled(enabled);
		user.setDeletedAt(deletedAt);
		return user;
	}

	// 会員の太郎さんを退会させるメソッド
	private void markActiveTaroAsDeleted() {
		User managedActiveTaro = entityManager.find(User.class, activeTaro.getId()); // これで managed になる

		managedActiveTaro.setEnabled(false);
		managedActiveTaro.setDeletedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

		entityManager.flush();
		entityManager.clear();
	}

	// ページネーションの動作を検証するために、ユーザーを追加するメソッド
	private void persistExtraUsers(int count, Role role) {
		for (int i = 0; i < count; i++) {
			entityManager.persist(createUser(
					"追加ユーザー" + i,
					"ツイカ ユーザー" + i,
					"extra" + i + "@example.com",
					LocalDate.of(1995, 1, 1),
					"テスター",
					role,
					true,
					null));
		}
		entityManager.flush();
		entityManager.clear();
	}

	// -------------- 各カラムの属性のリストにするメソッド --------------
	// PageのUserをidリストにする（検証用）
	private List<Integer> toIds(Page<User> page) {
		return page.getContent().stream().map(User::getId).toList();
	}

	// 順不同でIDセットが一致することを確認
	private void assertIdsEqualAsSet(List<Integer> expected, List<Integer> actual) {
		assertEquals(expected.size(), actual.size(), "件数が一致しません");
		assertTrue(actual.containsAll(expected), "expectedのIDがすべて含まれていません");
		assertTrue(expected.containsAll(actual), "余計なIDが含まれています");
		// ③ ★順番も同じ（ページング順序の担保）
		assertEquals(expected, actual, "順番を含めてIDリストが一致しません");
	}

	// deletedAtがnull（=Active）だけであることを確認（active系のクエリ用）
	private void assertAllActive(Page<User> page) {
		assertTrue(page.getContent().stream().allMatch(u -> u.getDeletedAt() == null),
				"deletedAt!=null のユーザーが混入しています");
	}

	// Like検索などで取得したUserが「全カラム正しい」ことを担保する（回帰テスト用）
	private void assertUserAllColumns(User expected, User actual) {
		assertNotNull(expected, "expected が null です");
		assertNotNull(actual, "actual が null です");

		assertAll("Userの全カラム一致",
				// Userのカラムを比較
				() -> assertEquals(expected.getId(), actual.getId(), "id"),
				() -> assertEquals(expected.getName(), actual.getName(), "name"),
				() -> assertEquals(expected.getFurigana(), actual.getFurigana(), "furigana"),
				() -> assertEquals(expected.getPostalCode(), actual.getPostalCode(), "postalCode"),
				() -> assertEquals(expected.getAddress(), actual.getAddress(), "address"),
				() -> assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber(), "phoneNumber"),
				() -> assertEquals(expected.getBirthday(), actual.getBirthday(), "birthday"),
				() -> assertEquals(expected.getOccupation(), actual.getOccupation(), "occupation"),
				() -> assertEquals(expected.getEmail(), actual.getEmail(), "email"),
				() -> assertEquals(expected.getPassword(), actual.getPassword(), "password"),

				// role（ManyToOne）: Roleそのもののequalsに依存せず、idとnameで担保
				() -> assertNotNull(actual.getRole(), "role が null です"),
				() -> assertEquals(expected.getRole().getId(), actual.getRole().getId(), "role.id"),
				() -> assertEquals(expected.getRole().getName(), actual.getRole().getName(), "role.name"),

				// 状態
				() -> assertEquals(expected.isEnabled(), actual.isEnabled(), "enabled"),
				() -> assertEquals(expected.getDeletedAt(), actual.getDeletedAt(), "deletedAt"),
				() -> assertEquals(expected.getDeletedByUser(), actual.getDeletedByUser(), "deletedByUser"),
				() -> assertEquals(expected.getDeleteReason(), actual.getDeleteReason(), "deleteReason"),
				() -> assertEquals(expected.getStripeCustomerId(), actual.getStripeCustomerId(), "stripeCustomerId"));
	}

	// -------------- 初期設定 --------------
	@BeforeEach
	public void setUp() {
		//-------------- roles --------------
		freeMemberRole = new Role();
		freeMemberRole.setName("ROLE_FREE_MEMBER");
		freeMemberRole = entityManager.persist(freeMemberRole);

		paidMemberRole = new Role();
		paidMemberRole.setName("ROLE_PAID_MEMBER");
		paidMemberRole = entityManager.persist(paidMemberRole);

		adminRole = new Role();
		adminRole.setName("ROLE_ADMIN");
		adminRole = entityManager.persist(adminRole);

		// -------------- users --------------
		activeTaro = entityManager.persist(createUser(
				"侍 太郎",
				"サムライ タロウ",
				"taro.samurai@example.com",
				LocalDate.of(1990, 1, 1),
				"エンジニア",
				freeMemberRole,
				true,
				null));

		jiro = entityManager.persist(createUser(
				"侍 次郎",
				"サムライ ジロウ",
				"jiro.samurai@example.com",
				LocalDate.of(1990, 2, 2),
				"デザイナー",
				paidMemberRole,
				true,
				null));

		hanako = entityManager.persist(createUser(
				"侍 花子",
				"サムライ ハナコ",
				"hanako.samurai@example.com",
				LocalDate.of(1990, 3, 3),
				"マーケティング",
				adminRole,
				true,
				null));

		entityManager.flush();
		entityManager.clear();
	}

	// --------------ここからテストコード--------------
	@Test
	@Description("findByEmail_指定したメールアドレスに一致するユーザーを取得できること")
	public void findByEmail_test1() throws Exception {
		User actualTaro = userRepository.findByEmail("taro.samurai@example.com");

		assertEquals(activeTaro, actualTaro);
	}

	@Test
	@Description("findByEmail_指定したメールアドレスに一致するユーザーが存在しない場合、nullを返すこと")
	public void findByEmail_test2() throws Exception {

		User actualNotFoundUser = userRepository.findByEmail("notfound.samurai@example.com");

		assertNull(actualNotFoundUser);
	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_ページング(1ページ目/2ページ目)の表示内容が正しいこと")
	public void findByNameLiekOrFuriganaLike_test1() throws Exception {

		// ページネーションの動作を検証するため、count=13とし、既存3件(Taro、Jiro、Hanako) + 13件 = 16件にする。
		persistExtraUsers(13, freeMemberRole);

		// ---------- 1ページ目 ----------
		Pageable pageable1 = PageRequest.of(0, 15);

		Page<User> expectedPage1 = userRepository.findAll(pageable1);
		Page<User> actualPage1 = userRepository.findByNameLikeOrFuriganaLike("%%", "%%", pageable1);

		assertAll("1ページ目",
				() -> assertEquals(15, actualPage1.getContent().size()),
				() -> assertEquals(16, actualPage1.getTotalElements()),
				() -> assertEquals(2, actualPage1.getTotalPages()),
				() -> assertIdsEqualAsSet(toIds(expectedPage1), toIds(actualPage1)));

		// ---------- 2ページ目 ---------- （別テストでもOK）
		Pageable pageable2 = PageRequest.of(1, 15);

		Page<User> expectedPage2 = userRepository.findAll(pageable2);
		Page<User> actualPage2 = userRepository.findByNameLikeOrFuriganaLike("%%", "%%", pageable2);

		assertAll("2ページ目",
				() -> assertEquals(1, actualPage2.getContent().size()),
				() -> assertEquals(16, actualPage1.getTotalElements()),
				() -> assertEquals(2, actualPage1.getTotalPages()),
				() -> assertIdsEqualAsSet(toIds(expectedPage2), toIds(actualPage2)));
	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_氏名またはフリガナでユーザーを検索し、ページングされた状態でユーザーを取得できること")
	public void findByNameLiekOrFuriganaLike_test2() throws Exception {

		Pageable pageable = PageRequest.of(0, 15);

		// 検索機能の検証
		Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);
		List<Integer> expectedIds = List.of(activeTaro.getId(), jiro.getId(), hanako.getId());
		List<Integer> actualIds = toIds(page);

		// ① OR検索（name側 or furigana側）
		assertAll("OR検索で取得すべき3件が正しいこと（順不同）",
				() -> assertIdsEqualAsSet(expectedIds, actualIds));

		// ② name側だけを有効にした場合（furiganaは絶対当たらないキーワード）
		Page<User> pageByNameOnly = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%存在しない%", pageable);
		List<Integer> idsByNameOnly = toIds(pageByNameOnly);

		assertAll("name側だけで同じ3件が取れること",
				() -> assertIdsEqualAsSet(expectedIds, idsByNameOnly));

		// ③ furigana側だけを有効にした場合（nameは絶対当たらないキーワード）
		Page<User> pageByFuriganaOnly = userRepository.findByNameLikeOrFuriganaLike("%存在しない%", "%サムライ%", pageable);
		List<Integer> idsByFuriganaOnly = toIds(pageByFuriganaOnly);

		assertAll("furigana側だけで同じ2件が取れること",
				() -> assertIdsEqualAsSet(expectedIds, idsByFuriganaOnly));

	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_取得したUserが全カラム正しいこと")
	public void findByNameLiekOrFuriganaLike_test3() throws Exception {

		Pageable pageable = PageRequest.of(0, 15);

		// 「太郎」にだけ当たるように絞る（1件ヒットさせるのが目的）
		Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%太郎%", "%存在しない%", pageable);

		assertAll(
				() -> assertEquals(1, page.getTotalElements(), "1件だけヒットする想定です"),
				() -> assertEquals(1, page.getContent().size(), "返却リストも1件の想定です"));

		User actual = page.getContent().get(0);

		// Like検索で返ってきたUserが、activeTaroの全カラムと一致することを保証
		assertUserAllColumns(activeTaro, actual);
	}

	@Test
	@Description("countByRole_Name_指定したロール名に紐づくユーザーのレコード数を取得できること")
	public void countByRole_Name_test1() throws Exception {

		long freeCount = userRepository.countByRole_Name("ROLE_FREE_MEMBER");

		assertEquals(1L, freeCount);
	}

	@Test
	@Description("findByEmailAndDeletedAtIsNull_指定したメールアドレスに一致した会員ユーザーを取得できること")
	public void findByEmailAndDeletedAtIsNull_test1() throws Exception {

		User actualTaro = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com").orElse(null);

		assertEquals(activeTaro, actualTaro);
	}

	@Test
	@Description("findByEmailAndDeletedAtIsNull_指定したメールアドレスに一致した会員ユーザーを取得できなかった場合に、Nullを取得できること（太郎さんが退会したらUserを取得できないこと）")
	public void findByEmailAndDeletedAtIsNull_test2() throws Exception {
		// 太郎さんが退会する
		markActiveTaroAsDeleted();

		// 太郎さんは退会済みで会員ではなくなっているので、Userを取得できない想定
		User actualTaro = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com").orElse(null);

		assertNull(actualTaro);
	}

	@Test
	@Description("findAllActive__ページングが正しく行われること")
	public void findAllActive_test1() throws Exception {

		// ページネーションの動作を検証するため、count=13とし、既存3件(Taro、Jiro、Hanako) + 13件 = 16件にする。
		persistExtraUsers(13, freeMemberRole);

		Pageable pageable = PageRequest.of(0, 15);
		Page<User> firstPage = userRepository.findAll(pageable);

		assertEquals(15, firstPage.getContent().size());
		assertEquals(16, firstPage.getTotalElements());
		assertEquals(2, firstPage.getTotalPages());
	}

	@Test
	@Description("findAllActive_会員ユーザーをページングされた状態で取得できること")
	public void findAllActive_test2() throws Exception {
		// 太郎さんが退会する
		markActiveTaroAsDeleted();

		Pageable pageable = PageRequest.of(0, 15);

		// 会員ユーザーを取得できるか検証
		Page<User> page = userRepository.findAllActive(pageable);

		List<User> actualList = page.getContent();

		// 会員であるjiro / hanako の2人
		assertEquals(2, actualList.size());

		// 退会者が含まれないこと（deletedAtがnullのみ）
		assertTrue(actualList.stream().allMatch(u -> u.getDeletedAt() == null));
	}

	@Test
	@Description("findActiveByNameLikeOrFuriganaLike_ページングが正しく行われること")
	public void findActiveByNameLikeOrFuriganaLike_test1() throws Exception {

		// 太郎さんが退会する（active対象から外れる）
		markActiveTaroAsDeleted();

		// 既存3件(Taro, Jiro, Hanako) + 13件 = 16件 だが、
		// Taroは退会済みなので active は 15件 の想定（Jiro/Hanako + extra13）
		persistExtraUsers(13, freeMemberRole);

		// ---------- 1ページ目 ----------
		Pageable pageable1 = PageRequest.of(0, 15);

		Page<User> expectedPage1 = userRepository.findAllActive(pageable1);
		Page<User> actualPage1 = userRepository.findActiveByNameLikeOrFuriganaLike("%%", "%%", pageable1);

		assertAll("1ページ目",
				() -> assertEquals(15, actualPage1.getContent().size()),
				() -> assertEquals(15, actualPage1.getTotalElements()),
				() -> assertEquals(1, actualPage1.getTotalPages()),
				() -> assertIdsEqualAsSet(toIds(expectedPage1), toIds(actualPage1)),
				() -> assertAllActive(actualPage1));

		// ---------- 2ページ目 ----------
		// active合計が15件なので、2ページ目は空になるのが正しい
		Pageable pageable2 = PageRequest.of(1, 15);

		Page<User> expectedPage2 = userRepository.findAllActive(pageable2);
		Page<User> actualPage2 = userRepository.findActiveByNameLikeOrFuriganaLike("%%", "%%", pageable2);

		assertAll("2ページ目",
				() -> assertEquals(0, actualPage2.getContent().size()),
				() -> assertEquals(0, expectedPage2.getContent().size()),
				() -> assertIdsEqualAsSet(toIds(expectedPage2), toIds(actualPage2)),
				() -> assertAllActive(actualPage2));

	}

	@Test
	@Description("findActiveByNameLikeOrFuriganaLike_氏名またはフリガナで会員ユーザーを検索し、ページングされた状態で取得できること")
	public void findActiveByNameLikeOrFuriganaLike_test2() throws Exception {

		// 太郎さんが退会する
		markActiveTaroAsDeleted();

		Pageable pageable = PageRequest.of(0, 15);

		// 検索機能の検証
		Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

		List<Integer> expectedIds = List.of(jiro.getId(), hanako.getId());
		List<Integer> actualIds = toIds(page);

		// ① OR検索（name側 or furigana側）
		assertAll("OR検索で取得すべき2件が正しいこと（順不同）",
				() -> assertIdsEqualAsSet(expectedIds, actualIds),
				() -> assertAllActive(page));

		// ② name側だけを有効にした場合（furiganaは絶対当たらないキーワード）
		Page<User> pageByNameOnly = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%存在しない%", pageable);
		List<Integer> idsByNameOnly = toIds(pageByNameOnly);

		assertAll("name側だけで同じ2件が取れること（たまたまfuriganaで取れていないことを担保）",
				() -> assertIdsEqualAsSet(expectedIds, idsByNameOnly),
				() -> assertAllActive(pageByNameOnly));

		// ③ furigana側だけを有効にした場合（nameは絶対当たらないキーワード）
		Page<User> pageByFuriganaOnly = userRepository.findActiveByNameLikeOrFuriganaLike("%存在しない%", "%サムライ%",
				pageable);
		List<Integer> idsByFuriganaOnly = toIds(pageByFuriganaOnly);

		assertAll("furigana側だけで同じ2件が取れること（たまたまnameで取れていないことを担保）",
				() -> assertIdsEqualAsSet(expectedIds, idsByFuriganaOnly),
				() -> assertAllActive(pageByFuriganaOnly));

	}

	@Test
	@Description("findAllIncludeDeleted_ユーザー一覧を退会日が新しい順に並べ替え、リスト化された状態で取得できること")
	public void findAllIncludeDeleted_test1() throws Exception {
		// 太郎さんが退会する
		markActiveTaroAsDeleted();

		List<User> actualList = userRepository.findAllIncludeDeleted();

		// 退会者が最低1件以上いること（太郎が退会済み）
		assertTrue(actualList.stream().anyMatch(u -> u.getDeletedAt() != null));

		// 合計3人（deletedTaro / jiro / hanako）
		assertEquals(3, actualList.size());

		// Repositoryは ORDER BY deletedAt ASC, id ASC なので deletedAt=null が先、deletedAt!=null が後ろに来る想定（退会者が末尾）
		User last = actualList.get(actualList.size() - 1);
		assertNotNull(last.getDeletedAt());
		assertEquals("taro.samurai@example.com", last.getEmail());
	}

	// -------------------------------以下AIの出力結果-----------------------------------
	@Test
	@Description("findByEmailAndDeletedAtIsNull_メールアドレスが一致し、かつ削除されていないユーザーを取得できること")
	public void findByEmailAndDeletedAtIsNull_test_1() throws Exception {
		var actualUser = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com");

		assertTrue(actualUser.isPresent());
		assertEquals(activeTaro, actualUser.get());
	}

	@Test
	@Description("findByEmailAndDeletedAtIsNull_削除されたユーザーのメールアドレスで検索した場合、空のOptionalが返ること")
	public void findByEmailAndDeletedAtIsNull_test_2() throws Exception {
		markActiveTaroAsDeleted();

		var actualUser = userRepository.findByEmailAndDeletedAtIsNull("taro.samurai@example.com");

		assertTrue(actualUser.isEmpty());
	}

	@Test
	@Description("findByEmailAndDeletedAtIsNull_存在しないメールアドレスで検索した場合、空のOptionalが返ること")
	public void findByEmailAndDeletedAtIsNull_test_3() throws Exception {
		var actualUser = userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com");

		assertTrue(actualUser.isEmpty());
	}

	@Test
	@Description("countByRole_Name_特定のロール名でユーザーを数えられること")
	public void countByRole_Name_test_1() throws Exception {
		long count = userRepository.countByRole_Name("ROLE_FREE_MEMBER");

		assertEquals(1, count);
	}

	@Test
	@Description("countByRole_Name_有料会員ロールでユーザーを数えられること")
	public void countByRole_Name_test_2() throws Exception {
		long count = userRepository.countByRole_Name("ROLE_PAID_MEMBER");

		assertEquals(1, count);
	}

	@Test
	@Description("countByRole_Name_管理者ロールでユーザーを数えられること")
	public void countByRole_Name_test_3() throws Exception {
		long count = userRepository.countByRole_Name("ROLE_ADMIN");

		assertEquals(1, count);
	}

	@Test
	@Description("findAllActive_ページングされたアクティブユーザー一覧を取得できること")
	public void findAllActive_test_1() throws Exception {
		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findAllActive(pageable);

		assertEquals(3, page.getTotalElements());
		assertTrue(page.getContent().stream().allMatch(u -> u.getDeletedAt() == null));
	}

	@Test
	@Description("findAllActive_削除されたユーザーを除いて取得できること")
	public void findAllActive_test_2() throws Exception {
		markActiveTaroAsDeleted();

		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findAllActive(pageable);

		assertEquals(2, page.getTotalElements());
		assertEquals(2, page.getContent().size());
		for (User user : page.getContent()) {
			assertNull(user.getDeletedAt());
		}
	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_名前で検索できること")
	public void findByNameLikeOrFuriganaLike_test_1() throws Exception {
		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%%", pageable);

		assertEquals(3, page.getContent().size());
		assertEquals("taro.samurai@example.com", page.getContent().get(0).getEmail());
	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_フリガナで検索できること")
	public void findByNameLikeOrFuriganaLike_test_2() throws Exception {
		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%%", "%サムライ%", pageable);

		assertEquals(3, page.getContent().size());
		assertEquals("jiro.samurai@example.com", page.getContent().get(1).getEmail());
	}

	@Test
	@Description("findByNameLikeOrFuriganaLike_複数の検索結果が返ることを確認")
	public void findByNameLikeOrFuriganaLike_test_3() throws Exception {
		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

		assertTrue(page.getContent().size() >= 2);
	}

	@Test
	@Description("findActiveByNameLikeOrFuriganaLike_退会ユーザーを除いて検索できること")
	public void findActiveByNameLikeOrFuriganaLike_test_1() throws Exception {
		markActiveTaroAsDeleted();

		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

		assertEquals(2, page.getContent().size());
		for (User user : page.getContent()) {
			assertNull(user.getDeletedAt());
		}
	}

	@Test
	@Description("findActiveByNameLikeOrFuriganaLike_複合検索で複数の結果を取得できること")
	public void findActiveByNameLikeOrFuriganaLike_test_2() throws Exception {
		Pageable pageable = PageRequest.of(0, 10);

		Page<User> page = userRepository.findActiveByNameLikeOrFuriganaLike("%侍%", "%サムライ%", pageable);

		assertEquals(3, page.getContent().size());
		for (User user : page.getContent()) {
			assertNull(user.getDeletedAt());
		}
	}

	@Test
	@Description("findAllIncludeDeleted_削除ユーザーを含めて取得できること")
	public void findAllIncludeDeleted_test_2() throws Exception {
		markActiveTaroAsDeleted();

		List<User> list = userRepository.findAllIncludeDeleted();

		assertTrue(list.stream()
				.anyMatch(u -> u.getEmail().equals("taro.samurai@example.com") && u.getDeletedAt() != null));
	}

	@Test
	@Description("findByEmail_複数ユーザーがいる場合、正しいユーザーを検索できること")
	public void findByEmail_test_3() throws Exception {
		User foundUser = userRepository.findByEmail("jiro.samurai@example.com");

		assertNotNull(foundUser);
		assertEquals("侍 次郎", foundUser.getName());
	}

}
