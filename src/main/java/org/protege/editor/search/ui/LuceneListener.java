package org.protege.editor.search.ui;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public interface LuceneListener {

    void searchStarted(LuceneEvent event);

    void searchFinished(LuceneEvent event);

}
