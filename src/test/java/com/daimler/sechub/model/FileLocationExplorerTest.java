// SPDX-License-Identifier: MIT
package com.daimler.sechub.model;

import static com.daimler.sechub.model.TestResourceHelper.getEnsuredTestPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FileLocationExplorerTest {

	private FileLocationExplorer explorerToTest;

	@Before
	public void before() {
		explorerToTest = new FileLocationExplorer();
		
	}
	
	@Test
	public void scenario1_projects_TestMe_java_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "TestMe.java";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(1,found.size());
		assertEquals(expectedFile, found.get(0));
	}
	
	@Test
	public void scenario1_projects_example_TestMe_java_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "example/TestMe.java";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(1,found.size());
		assertEquals(expectedFile, found.get(0));
	}
	
	@Test
	public void scenario1_projects_source_TestMe_c_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "source/TestMe.c";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(1,found.size());
		assertEquals(expectedFile, found.get(0));
	}
	
	@Test
	public void scenario1_projects_TestMe_c_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "TestMe.c";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(1,found.size());
		assertEquals(expectedFile, found.get(0));
	}
	
	@Test
	public void scenario1_projects_com_example_TestMe_java_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "com/example/TestMe.java";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(1,found.size());
		assertEquals(expectedFile, found.get(0));
	}
	
	@Test
	public void scenario1_projects_SameName_java_found() throws Exception {
		/* prepare */
		Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
		Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
		Path expectedFile1 = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/SameName.java");
		Path expectedFile2 = getEnsuredTestPath("explorer/scenario1/project1/src/test/java/com/example/subpackage/SameName.java");
		Path unExpectedFile3 = getEnsuredTestPath("SameName.java");
		
		explorerToTest.getSearchFolders().add(project1);
		explorerToTest.getSearchFolders().add(project2);
		
		/* execute */
		String locationString = "SameName.java";
		List<Path> found = explorerToTest.searchFor(locationString);
		
		/* test */
		assertEquals(2,found.size());
		assertTrue(found.contains(expectedFile1));
		assertTrue(found.contains(expectedFile2));
		assertFalse(found.contains(unExpectedFile3));
	}


}
