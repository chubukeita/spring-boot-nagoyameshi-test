package com.example.nagoyameshi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.form.UserWithdrawalForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.error.AlreadyEnabledException;
import com.example.nagoyameshi.service.error.RejoinUserNotFoundException;

@Service
public class UserService {
	private static final String ROLE_NAME = "ROLE_FREE_MEMBER";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	//	@Transactional
	public User createUser(SignupForm signupForm) {

		User user = new User();
		Role role = roleRepository.findByName(ROLE_NAME);

		// setはまとめて書く（リファクタリングの観点から、ユーザーの属性を一括で設定する方がコードの見通しが良くなるため）
		// カラム順にするのもありだが、set→if→setの順番にすると処理が分散して見にくくなる
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setOccupation(signupForm.getOccupation());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(false);

		// occupationは空文字登録、birthdayはnull登録
		if (!signupForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(signupForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		}

		// 認知的複雑性を減らすため、elseの処理は削除して、birthdayはnullのままにする

		return userRepository.save(user);

	}

	// userFormでリファクタリング
	//	@Transactional
	public void updateUser(UserEditForm userEditForm, User user) {

		// userEditForm.getBirthday()が空か、nullか？
		System.out.println("userEditForm.getBirthday():" + userEditForm.getBirthday());
		System.out.println("①userEditForm.getBirthday()はnullかどうか");
		System.out.println(userEditForm.getBirthday() == null);
		System.out.println("②userEditForm.getBirthday()は空文字かどうか（文字数が0文字かどうか）");
		System.out.println(userEditForm.getBirthday().isEmpty());
		System.out.println("user.getBirthday():" + user.getBirthday());

		user.setName(userEditForm.getName());
		user.setFurigana(userEditForm.getFurigana());
		user.setPostalCode(userEditForm.getPostalCode());
		user.setAddress(userEditForm.getAddress());
		user.setPhoneNumber(userEditForm.getPhoneNumber());
		user.setOccupation(userEditForm.getOccupation());
		user.setEmail(userEditForm.getEmail());

		// user.setBirthday(null);をしたらsetBirthday(null)を比較して、誕生日が未設定になる理由

		if (!userEditForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(userEditForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		} else {
			user.setBirthday(null);
		}

		// userのbirthdayにnullをsetしたのちに、userEditForm.getBirthday()が空か、nullか？
		// user.setBirthday(null);
		// System.out.println("userEditForm.getBirthday():" + userEditForm.getBirthday());
		// System.out.println("userEditForm.getBirthday()=null" + userEditForm.getBirthday() == null);
		// System.out.println("userEditForm.getBirthday()は文字数が0文字かどうか" + userEditForm.getBirthday().isEmpty());
		// System.out.println("user.getBirthday():" + user.getBirthday());

		userRepository.save(user);

	}

	// メールアドレスが登録済みかどうかをチェックする
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}

	// ユーザーを有効にする
	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(user);
	}

	// 再設定パスワードを有効にする
	@Transactional
	public void enablePassword(PasswordResetForm passwordResetForm, User user) {
		user.setPassword(passwordEncoder.encode(passwordResetForm.getPassword()));
		userRepository.save(user);
	}

	// すべてのユーザーをページングされた状態で取得する
	public Page<User> findAllUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	// 指定されたキーワードを氏名またはフリガナに含むユーザーを、ページングされた状態で取得する
	public Page<User> findUsersByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable) {
		return userRepository.findByNameLikeOrFuriganaLike("%" + nameKeyword + "%", "%" + furiganaKeyword + "%",
				pageable);
	}

	// 指定したidを持つユーザーを取得する
	public Optional<User> findUserById(Integer id) {
		return userRepository.findById(id);
	}

	// 指定したロール名に紐づくユーザーのレコード数を取得する
	public long countUsersByRole_Name(String roleName) {
		return userRepository.countByRole_Name(roleName);
	}

	// メールアドレスが変更されたかどうかをチェックする
	public boolean isEmailChanged(UserEditForm userEditForm, User user) {
		return !userEditForm.getEmail().equals(user.getEmail());
	}

	// 指定したメールアドレスを持つユーザーを取得する
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// 指定したメールアドレスを持つ退会ユーザーを取得する
	public Optional<User> findDeletedUserByEmail(String email) {
		return userRepository.findByEmailAndDeletedAtIsNotNull(email);
	}

	@Transactional
	public void saveStripeCustomerId(User user, String stripeCustomerId) {
		user.setStripeCustomerId(stripeCustomerId);
		userRepository.save(user);
	}

	@Transactional
	public void updateRole(User user, String roleName) {
		Role role = roleRepository.findByName(roleName);
		user.setRole(role);
		userRepository.save(user);
	}

	// コントローラ用：未退会ユーザーをOptionalで返す（存在確認はController側）
	public Optional<User> findActiveByEmail(String email) {
		return userRepository.findByEmailAndDeletedAtIsNull(email);
	}

	@Transactional
	public void withdrawal(UserWithdrawalForm userWithdrawalForm, User user, String reason) {

		user.setDeletedAt(LocalDateTime.now());
		user.setDeletedByUser(true);
		user.setEnabled(false);
		user.setDeleteReason(reason); // 補足は保存したければカラム追加 or ログ記録
		// user.setDeleteReason(userWithdrawalForm.getDeleteReason()); // 本当はFormからdeletedReasonを受け付けるべきこと

		userRepository.save(user);
	}

	@Transactional
	public void rejoinByEmail(String email) {
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new RejoinUserNotFoundException();
		}

		// 正しくない書き方ではかもしれない→調査が必要
		if (user.isEnabled()) {
			throw new AlreadyEnabledException();
		}

		user.setDeletedAt(null);
		user.setDeletedByUser(null);
		user.setDeleteReason(null);

		user.setEnabled(true);
		//　Transactionと明示的に書いているから、UPDATEまで保証されている。
		// 通信確立されていて、findで行をとってくるから、saveメソッドは不要。
		//　userRepository.save(user); // いらない Select For Updateでロックされるため、更新の必要がない場合はsaveしない方が良い
	}

	// 認証情報のロールを更新する
	public void refreshAuthenticationByRole(String newRole) {
		// 現在の認証情報を取得する
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

		// 新しい認証情報を作成する
		List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
		simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(),
				currentAuthentication.getCredentials(), simpleGrantedAuthorities);

		// 認証情報を更新する
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
	}
}
