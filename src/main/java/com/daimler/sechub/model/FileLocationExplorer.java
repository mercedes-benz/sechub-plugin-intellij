// SPDX-License-Identifier: MIT
package com.daimler.sechub.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileLocationExplorer {

	private List<Path> searchFolders = new ArrayList<>();

	public List<Path> getSearchFolders() {
		return searchFolders;
	}

	/**
	 * Searches for given location string
	 * 
	 * @param location represents known location to search for inside defined search folders
	 * @return list of matching files, never <code>null</code>
	 * @throws IOException
	 */
	public List<Path> searchFor(String location) throws IOException {
		List<Path> result = new ArrayList<>();

		for (Path searchFolder : searchFolders) {
			searchFilesRecursive(location, searchFolder,result);
		}

		return result;
	}

	private void searchFilesRecursive(String location, Path searchFolder, List<Path> result) throws IOException {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:.*" + location);
		Collection<Path> found = find(searchFolder, matcher);
		for (Path path: found) {
			result.add(path);
		}
	}

	protected static Collection<Path> find(Path searchDirectory, PathMatcher matcher) throws IOException {
		try (Stream<Path> files = Files.walk(searchDirectory)) {
			return files.filter(matcher::matches).collect(Collectors.toList());
		}

	}

}
