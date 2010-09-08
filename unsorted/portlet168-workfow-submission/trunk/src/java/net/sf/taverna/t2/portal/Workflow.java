package net.sf.taverna.t2.portal;

/**
 * Represents all information we have about a workflow input
 * port (such as name, depth and annotations), read from the
 * workflow file.
 *
 * @author Alex Nenadic
 */
public class Workflow {

    // Workflow author read from annotations.
    private String author;

    // Workflow title read from annotations.
    private String title;

    // Workflow description read from annotations.
    private String description;

    public Workflow(){
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
