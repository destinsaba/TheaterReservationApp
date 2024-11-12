package com.project.java_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.java_backend.model.Movie;
import com.project.java_backend.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

	@Autowired
	private MovieService movieService;

	// Get all movies for RUs
	@GetMapping
	public List<Movie> getAllMovies() {
		return movieService.getAllMovies();
	}

	// Get public movies
	@GetMapping(value="/public", consumes="application/json", produces="application/json")
	public ResponseEntity<List<Movie>>getAllPublicMovies() {
		List<Movie> publicMovies = movieService.getPublicMovies();
		return ResponseEntity.ok(publicMovies);
	}

	// Get movie by id
	@GetMapping(value="/{id}", consumes="application/json", produces="application/json")
	public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
		Movie movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	// Create movie
	@PostMapping(consumes="application/json", produces="application/json")
	  public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        Movie createdMovie = movieService.createMovie(movie);
        return ResponseEntity.ok(createdMovie);
    }
	
	// Update movie
	@PutMapping(value="/{id}", consumes="application/json", produces="application/json")
	public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movieDetails) {
		Movie upatedMovie = movieService.updateMovie(id, movieDetails);
		return ResponseEntity.ok(upatedMovie);
	}

	// Make movie public
	@PutMapping(value="/makepublic/{id}", consumes="application/json", produces="application/json")
	public ResponseEntity<Movie> makeMoviePublic(@PathVariable Long id) {
		Movie publicMovie = movieService.makeMoviePublic(id);
		return ResponseEntity.ok(publicMovie);
	}

	// Delete movie
	@DeleteMapping(value="/{id}", consumes="application/json", produces="application/json")
	public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}

}
