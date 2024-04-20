package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	// @formatter:off
	private List<String> operations = List.of(
		"1) Add a project",
		"2) List projects",
		"3) Select a project",
		"4) Update project details",
		"5) Delete a project"
	);
	// @formatter:on
	
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project currentProject;

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
		
	}

	private void processUserSelections() {
		boolean done = false;
		
		while (!done) {
			try {
				int selection = getUserSelection();
				
				switch(selection) {
				case -1:
					done = exitMenu();
					break;
				case 1:
					createProject();
					break;
				case 2:
					listProjects();
					break;
				case 3:
					selectProject();
					break;
				case 4:
					updateProjectDetails();
					break;
				case 5:
					deleteProject();
					break;
				default:
					System.out.println("\n " + selection + " is not a valid selection. Try again.");
				
				}
			} catch(Exception e) {
				System.out.println("\nError: " + e + ". Try again.");
			}
			
			
		}
		
	}

	private void deleteProject() {
		listProjects();
		
		Integer projectId = getIntInput("Enter the project ID to delete");
		
		if(Objects.nonNull(projectId)) {
			projectService.deleteProject(projectId);
			
			System.out.println("Your project has been deleted.");
		}
		
		if(Objects.nonNull(currentProject) && currentProject.getProjectId().equals(projectId)) {
			currentProject = null;
		}
		
		
	}

	private void updateProjectDetails() {
		if(Objects.isNull(currentProject)) {
			System.out.println("\nPlease select a project.");
			return;
		}
		
		String projectName = getStringInput("Enter the project name [" + currentProject.getProjectName() + "]. Press enter if unchanged");
		BigDecimal projectEstimatedHours = getDecimalInput("Enter the estimated hours [" + currentProject.getEstimatedHours() + "]. Press enter if unchanged");
		BigDecimal projectActualHours = getDecimalInput("Enter the actual hours [" + currentProject.getActualHours() + "]. Press enter if unchanged");
		Integer projectDifficulty = null;
		boolean valid = false;
		do {
			projectDifficulty = getIntInput("Enter the project difficulty [" + currentProject.getDifficulty() + "]. Press enter if unchanged");
			
			if(Objects.isNull(projectDifficulty)) {
				valid = true;
			} else if(projectDifficulty >= 1 && projectDifficulty <= 5) {
				valid = true;
			} else {
				System.out.println("\nInvalid difficulty entry. Try again.");
			}

		} while(!valid);
		
		String projectNotes = getStringInput("Enter the project notes [" + currentProject.getNotes() + "]. Press enter if unchanged");
		
		Project project = new Project();
		project.setProjectName(Objects.isNull(projectName) ? currentProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(projectEstimatedHours) ? currentProject.getEstimatedHours() : projectEstimatedHours);
		project.setActualHours(Objects.isNull(projectActualHours) ? currentProject.getActualHours() : projectActualHours);
		project.setDifficulty(Objects.isNull(projectDifficulty) ? currentProject.getDifficulty() : projectDifficulty);
		project.setNotes(Objects.isNull(projectNotes) ? currentProject.getNotes() : projectNotes);
		project.setProjectId(currentProject.getProjectId());
		
		projectService.modifyProjectDetails(project);
		currentProject = projectService.fetchProjectById(currentProject.getProjectId());
	}

	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project.");
		
		currentProject = null;
		
		currentProject = projectService.fetchProjectById(projectId);
	}

	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		System.out.println("\nProjects:");
		
		projects.forEach(project -> System.out
				.println("\t" + project.getProjectId()
				+ ": " + project.getProjectName()));
	}

	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty;
		boolean valid = false;
		do {
			difficulty = getIntInput("Enter the project difficulty (1-5)");
			
			if(difficulty >= 1 && difficulty <= 5) {
				valid = true;
			} else {
				System.out.println("\nInvalid difficulty entry. Try again.");
			}

		} while(!valid);
		
		String notes = getStringInput("Enter the project notes");
		
		Project project = new Project();
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project);
		System.out.println("You have successfully created project: " + dbProject);
	}

	private boolean exitMenu() {
		System.out.println("\nExiting menu.");
		return true;
	}

	private int getUserSelection() {
		printOperation();
		
		Integer input = getIntInput("Enter a menu selection");
		
		return Objects.isNull(input) ? -1 : input;
	}

	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return Integer.valueOf(input);
		} catch(NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
		
	}
	
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return  new BigDecimal(input).setScale(2);
		} catch(NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		
		String input = scanner.nextLine();
		
		if (Objects.isNull(input)) {
			return null;
		}
		
		return input.isBlank() ? null : input.trim();
	}

	private void printOperation() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit:");
		
		operations.forEach(line -> System.out.println("\t" + line));
		
		if(Objects.isNull(currentProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + currentProject);
		}
	}

}
