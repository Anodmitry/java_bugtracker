package entities;

public class Issue {
	//œŒÃ≈Õﬂ“‹ ÃŒƒ»‘» ¿“Œ–€, Ì‡ÍË‰‡Ú¸ set Ë get
	private String id;
	private Project project;
	private User user;
	private String date_of_birth;
	private String discription;
	//int state;//maybe enum
	
	public Issue(String id, Project project, User user, String date_of_birth, String discription/*, int state*/){
		this.setId(id);
		this.setProject(project);
		this.setUser(user);
		this.setDate_of_birth(date_of_birth);
		this.setDiscription(discription);
		//this.state = state;
	}
	
	@Override public String toString() {
		return "\n\t<ID: " + this.getId() + "\n\tProject: " + this.getProject().getId() + "\n\tUser: " + this.getUser().getId() + "\n\tDate: " + this.getDate_of_birth() + "\n\tDiscription: " + this.getDiscription() + ">";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public String getDate_of_birth() {
		return date_of_birth;
	}

	public void setDate_of_birth(String date_of_birth) {
		this.date_of_birth = date_of_birth;
	}
}
