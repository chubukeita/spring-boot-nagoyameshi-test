package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.record.AdminRestaurantListCond;
import com.example.nagoyameshi.record.RestaurantListCond;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantNavService {
	private final RestaurantRepository restaurantRepository;

	public RestaurantNavService(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}

	public record PreviewNext(Integer previewId, Integer nextId) {
	}

	public PreviewNext findNeighborsNameOnly(int currentId, String keyword) {
		var pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));

		// findByNameLike はワイルドカード % を自分で付ける必要あり
		String pattern = (keyword == null || keyword.isBlank()) ? "%" : "%" + keyword + "%";

		Page<Restaurant> page = restaurantRepository.findByNameLike(pattern, pageable);

		List<Integer> ids = page.getContent().stream().map(Restaurant::getId).toList();

		int idx = ids.indexOf(currentId);
		if (idx < 0)
			return new PreviewNext(null, null);

		Integer preview = (idx > 0) ? ids.get(idx - 1) : null;
		Integer next = (idx + 1 < ids.size()) ? ids.get(idx + 1) : null;
		return new PreviewNext(preview, next);
	}

	public PreviewNext findNeighbors(int currentId, String keyword, Integer categoryId, Integer price, String order) {

		// 一覧と同じ分岐で、Pageable.unpaged() を渡して「全件(条件付き)」取得
		List<Integer> ids = fetchIds(keyword, categoryId, price, order);

		int index = ids.indexOf(currentId);
		if (index < 0)
			return new PreviewNext(null, null);

		Integer preview = (index > 0) ? ids.get(index - 1) : null;
		Integer next = (index + 1 < ids.size()) ? ids.get(index + 1) : null;
		return new PreviewNext(preview, next);
	}

	// 現在IDに対する「戻るURL（正しいページ）」を作成
	public String buildBackUrlForNameOnly(int currentId, AdminRestaurantListCond cond, int pageSize, String basePath) {
		// 一覧のデフォルトに合わせる
		var pageableIdAsc = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));

		String keyword = (cond != null) ? cond.keyword() : null;

		Page<Restaurant> page;
		if (keyword == null || keyword.isBlank()) {
			// キーワードなし → 全件を id ASC で
			page = restaurantRepository.findAll(pageableIdAsc);
		} else {
			// キーワードあり → 名前部分一致を id ASC で
			String pattern = "%" + keyword + "%";
			page = restaurantRepository.findByNameLike(pattern, pageableIdAsc);
		}

		List<Integer> ids = page.getContent().stream().map(Restaurant::getId).toList();
		int index = ids.indexOf(currentId);
		int pageNumber = (index >= 0) ? index / pageSize : 0;

		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(basePath)
				.queryParam("page", pageNumber)
				.queryParam("size", pageSize);

		if (keyword != null && !keyword.isBlank()) {
			uriComponentsBuilder.queryParam("keyword", keyword);
		}
		return uriComponentsBuilder.toUriString();

	}

	// 現在IDに対する「戻るURL（正しいページ）」を作成
	public String buildBackUrlFor(int currentId, RestaurantListCond cond, int pageSize, String basePath) {
		// 条件がnullでも安全に扱う
		String keyword = cond != null ? cond.keyword() : null;
		Integer category = cond != null ? cond.categoryId() : null;
		Integer price = cond != null ? cond.price() : null;
		String order = cond != null ? cond.order() : null;

		List<Integer> ids = fetchIds(keyword, category, price, order);
		int index = ids.indexOf(currentId);
		int page = (index >= 0) ? index / pageSize : 0;

		UriComponentsBuilder uniComponentsBuilder = UriComponentsBuilder.fromPath(basePath)
				.queryParam("page", page)
				.queryParam("size", pageSize);

		if (keyword != null && !keyword.isBlank())
			uniComponentsBuilder.queryParam("keyword", keyword);
		if (category != null)
			uniComponentsBuilder.queryParam("categoryId", category);
		if (price != null)
			uniComponentsBuilder.queryParam("price", price);
		if (order != null && !order.isBlank())
			uniComponentsBuilder.queryParam("order", order);

		return uniComponentsBuilder.toUriString();
	}

	// null/空文字の order を既定値に正規化
	private String normalizeOrder(String ord) {
		return (ord == null || ord.isBlank()) ? "createdAtDesc" : ord;
	}

	private List<Integer> fetchIds(String keyword, Integer categoryId, Integer price, String ord) {
		String order = normalizeOrder(ord);
		Page<Restaurant> page;

		if (keyword != null && !keyword.isBlank()) {
			page = switch (order) {
			case "lowestPriceAsc" -> restaurantRepository
					.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
							keyword, keyword, keyword, Pageable.unpaged());
			case "ratingDesc" -> restaurantRepository
					.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
							keyword, keyword, keyword, Pageable.unpaged());
			case "popularDesc" -> restaurantRepository
					.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
							keyword, keyword, keyword, Pageable.unpaged());
			default -> restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(
					keyword, keyword, keyword, Pageable.unpaged());
			};

		} else if (categoryId != null) {
			page = switch (order) {
			case "lowestPriceAsc" -> restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(categoryId,
					Pageable.unpaged());
			case "ratingDesc" -> restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(categoryId,
					Pageable.unpaged());
			case "popularDesc" -> restaurantRepository.findByCategoryIdOrderByReservationCountDesc(categoryId,
					Pageable.unpaged());
			default -> restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, Pageable.unpaged());
			};

		} else if (price != null) {
			page = switch (order) {
			case "lowestPriceAsc" -> restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(price,
					Pageable.unpaged());
			case "ratingDesc" -> restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(price,
					Pageable.unpaged());
			case "popularDesc" -> restaurantRepository.findByLowestPriceLessThanEqualOrderByReservationCountDesc(price,
					Pageable.unpaged());
			default -> restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(price,
					Pageable.unpaged());
			};

		} else {
			page = switch (order) {
			case "lowestPriceAsc" -> restaurantRepository.findAllByOrderByLowestPriceAsc(Pageable.unpaged());
			case "ratingDesc" -> restaurantRepository.findAllByOrderByAverageScoreDesc(Pageable.unpaged());
			case "popularDesc" -> restaurantRepository.findAllByOrderByReservationCountDesc(Pageable.unpaged());
			default -> restaurantRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged());
			};
		}

		// IDだけ抜き出す
		return page.getContent().stream()
				.map(Restaurant::getId)
				.toList();
	}
}
