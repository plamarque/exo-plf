import org.exoplatform.webui.core.model.SelectItemOption ;
import org.exoplatform.webui.core.model.SelectItemCategory ;
import java.util.List;
import java.util.ArrayList;

  List options = new ArrayList() ;
  
  SelectItemCategory item  = new SelectItemCategory("Default");
  item.addSelectItemOption(new SelectItemOption("Portal template",
                           "war:/groovy/portal/webui/component/view/UIPortal.gtmpl",
                           "Description","Default"));  
  options.add(item)

return options;


