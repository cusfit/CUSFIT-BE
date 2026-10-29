package com.cusfit.cusfitbe.recipe.controller;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.recipe.controller.docs.RecipeControllerDocs;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import com.cusfit.cusfitbe.recipe.service.RecipeWriteService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/api/dev/recipes")
public class RecipeController implements RecipeControllerDocs {

	private final RecipeReadService readService;
	private final RecipeWriteService writeService;

	public RecipeController(RecipeReadService readService, RecipeWriteService writeService) {
		this.readService = readService;
		this.writeService = writeService;
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<RecipeResponse>> create(@Valid @RequestBody CreateRecipeRequest request) {
		RecipeResponse response = writeService.create(request);
		return ResponseEntity.created(URI.create("/api/dev/recipes/" + response.id()))
				.body(ApiResponse.ok(response));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<RecipeResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(readService.getById(id)));
	}
}
