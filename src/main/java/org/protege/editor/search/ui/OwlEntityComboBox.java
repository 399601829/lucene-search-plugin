package org.protege.editor.search.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class OwlEntityComboBox extends JComboBox<OWLEntity> {
    private static final long serialVersionUID = -4520939367481847482L;
    private static final int MAX_VISIBLE_ROWS = 15;
    private OWLEditorKit editorKit;
    private OWLEntityFinder entityFinder;
    private List<OWLEntity> items;
    private DefaultComboBoxModel model;
    private JTextField editorTextField;
    private ItemListener listener;
    private OWLEntity lastSelectedEntity;

    /**
     * Constructor
     *
     * @param editorKit    OWL Editor Kit
     * @param items List of items to include in the combo box
     */
    public OwlEntityComboBox(OWLEditorKit editorKit, List<OWLEntity> items) {
        super(items.toArray(new OWLEntity[items.size()]));
        this.items = checkNotNull(items);
        this.editorKit = checkNotNull(editorKit);
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
        model = (DefaultComboBoxModel) getModel();
        initUi();
    }

    private void initUi() {
        setEditable(true);
        setBackground(Color.WHITE);
        setMaximumRowCount(MAX_VISIBLE_ROWS);
        setRenderer(new OWLCellRenderer(editorKit));
        setEditor(new OwlEntityComboBoxEditor());
        insertItemAt(null, 0);
        setSelectedIndex(0);
        editorTextField = (JTextField) getEditor().getEditorComponent();
        editorTextField.setBorder(LuceneUiHelper.Utils.MATTE_BORDER);
        editorTextField.addKeyListener(keyAdapter);
    }

    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent event) {
            int keycode = event.getKeyCode();
            if (keycode == KeyEvent.VK_ESCAPE) {
                hidePopup();
            } else if(keycode == KeyEvent.VK_DOWN) {
                JList list = getPopupList();
                if(list.getSelectedIndex()+1 < model.getSize()) {
                    list.setValueIsAdjusting(true);
                    Object obj = list.getModel().getElementAt(list.getSelectedIndex()+1);
                    if(obj == null) {
                        list.setSelectedIndex(0);
                    } else {
                        list.setSelectedValue(obj, true);
                    }
                    list.setValueIsAdjusting(false);
                }
            } else if(keycode == KeyEvent.VK_UP) {
                JList list = getPopupList();
                if(list.getSelectedIndex()-1 >= 0) {
                    list.setValueIsAdjusting(true);
                    Object obj = list.getModel().getElementAt(list.getSelectedIndex()-1);
                    if(obj == null) {
                        list.setSelectedIndex(0);
                    } else {
                        list.setSelectedValue(obj, true);
                    }
                    list.setValueIsAdjusting(false);
                }
                editorTextField.setFocusTraversalKeysEnabled(true);
            } else if (keycode == KeyEvent.VK_ENTER) {
                setSelectedItem(lastSelectedEntity);
            } else {
                if (!editorTextField.getText().isEmpty()) {
                    SwingUtilities.invokeLater(() -> filter(editorTextField.getText()));
                } else {
                    removeItemListener(listener);
                    model.removeAllElements();
                    model.addElement(null);
                    items.forEach(model::addElement);
                    editorTextField.setText("");
                    addItemListener(listener);
                    if(getVisibleRows() < MAX_VISIBLE_ROWS) {
                        resetPopup();
                    }
                }
            }
        }
    };

    private void filter(String inputText) {
        if (!isPopupVisible()) {
            showPopup();
        }
        List<OWLEntity> filteredItems = findMatchingEntities(inputText);
        removeItemListener(listener);
        model.removeAllElements();
        if (filteredItems.size() > 0) {
            filteredItems.forEach(model::addElement);
            int visibleRows = getVisibleRows();
            if(visibleRows < filteredItems.size() && visibleRows < MAX_VISIBLE_ROWS) {
                resetPopup();
            }
        }
        ((JTextField) getEditor().getEditorComponent()).setText(inputText);
        addItemListener(listener);
    }

    private List<OWLEntity> findMatchingEntities(String inputText) {
        List<OWLEntity> matchingEntities = new ArrayList<>();
        matchingEntities.addAll(entityFinder.getMatchingOWLAnnotationProperties(inputText));
        matchingEntities.addAll(entityFinder.getMatchingOWLDataProperties(inputText));
        matchingEntities.addAll(entityFinder.getMatchingOWLObjectProperties(inputText));
        return matchingEntities;
    }

    private int getVisibleRows() {
        JList list = getPopupList();
        return list.getLastVisibleIndex() - list.getFirstVisibleIndex() + 1;
    }

    private JList getPopupList() {
        ComboPopup popup = (ComboPopup) getUI().getAccessibleChild(this, 0);
        return popup.getList();
    }

    private void resetPopup() {
        hidePopup();
        showPopup();
    }

    @Override
    public Object getSelectedItem() {
        if(dataModel.getSelectedItem() instanceof OWLEntity) {
            return dataModel.getSelectedItem();
        } else {
            return lastSelectedEntity;
        }
    }

    @Override
    public void setSelectedItem(Object object) {
        if(object instanceof OWLEntity) {
            lastSelectedEntity = (OWLEntity) object;
        }
        super.setSelectedItem(object);
    }

    @Override
    public void addItemListener(ItemListener listener) {
        if(listener != null) {
            this.listener = listener;
        }
        super.addItemListener(listener);
    }
}