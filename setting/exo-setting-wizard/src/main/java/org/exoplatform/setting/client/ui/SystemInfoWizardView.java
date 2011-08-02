package org.exoplatform.setting.client.ui;

import org.exoplatform.setting.client.WizardModule;
import org.exoplatform.setting.client.data.InvalidWizardViewFieldException;
import org.exoplatform.setting.shared.WizardUtility;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View corresponding to <b>STEP 0 - Setup</b>
 * 
 * @author Clement
 *
 */
public class SystemInfoWizardView extends WizardView {
  
  public SystemInfoWizardView(WizardModule gui, int stepNumber) {
    super(gui, stepNumber);
  }

  @Override
  protected String getWizardTitle() {
    return constants.systemInfoTitle();
  }

  @Override
  protected String getWizardDescription() {
    return constants.systemInfoDescription();
  }

  @Override
  protected Widget buildStepToolbar() {
    
    Grid gridToolbar = new Grid(1, 2);
    gridToolbar.setWidth("100%");
    gridToolbar.getColumnFormatter().setWidth(0, "100%");
    
    // Add language form
    ListBox languages = new ListBox();
    languages.addItem(constants.english());
    languages.setValue(0, "en");
    languages.addItem(constants.francais());
    languages.setValue(1, "fr");
    languages.setWidth("150px");
    languages.setSelectedIndex(getIndexByLanguagesValue(languages, Window.Location.getParameter("locale")));
    languages.addChangeHandler(new ChangeHandler() {

      // Change language
      public void onChange(ChangeEvent arg0) {
        ListBox lgs = (ListBox) arg0.getSource();
        String newLanguage = lgs.getValue(lgs.getSelectedIndex());
        String oldLanguage = Window.Location.getParameter("locale");
        String newUrl = WizardUtility.buildLocaleUrl(Window.Location.getHref(), Window.Location.getQueryString(), oldLanguage, newLanguage);
        Window.Location.replace(newUrl);
      }
    });
    Grid gridLanguages = new Grid(1, 2);
    gridLanguages.setCellSpacing(3);
    gridLanguages.setHTML(0, 0, constants.chooseLanguage());
    gridLanguages.setWidget(0, 1, languages);
    
    gridToolbar.setWidget(0, 0, gridLanguages);
    gridToolbar.setWidget(0, 1, prepareNextButton(constants.start()));
    
    return gridToolbar;
  }

  @Override
  protected Widget buildStepContent() {

    Grid systemInfoOptions = new Grid(3, 3);
    systemInfoOptions.setCellSpacing(6);
    systemInfoOptions.setHTML(0, 0, "<b>App Server: </b>");
    systemInfoOptions.setHTML(0, 1, "Apache Tomcat");
    systemInfoOptions.setHTML(1, 0, "<b>JVM: </b>");
    systemInfoOptions.setHTML(1, 1, "Sun 1.6");
    
    return systemInfoOptions;
  }

  @Override
  protected void storeDatas(int toStep) {
    
    gui.storeDatas(null, toStep);
  }
  
  @Override
  protected void verifyDatas() throws InvalidWizardViewFieldException {
    
  }

  @Override
  public void initScreen() {
  }
  
  /**
   * Return an index corresponding of the value
   * @param languages
   * @param language
   * @return
   */
  public int getIndexByLanguagesValue(ListBox languages, String language) {
    if(languages != null) {
      for(int i=0; i<languages.getItemCount(); i++) {
        if(languages.getValue(i).equals(language)) {
          return i;
        }
      }
    }
    return 0;
  }
  
  
}
