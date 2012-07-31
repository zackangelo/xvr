package com.avian.xvr.util.config;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;

import java.util.*;

public class Application extends ApplicationWindow {
	
	public Application() { 
		super(null);
		
//		pageMap = new HashMap<ConfiguratonPage,Composite>();
	}
	
	private ConfigurationSchema schema;
	private static final String SCHEMA_FILE_NAME = 
		"configurationSchema.xml";
	
	private Font pageTierOneFont,pageTierTwoFont,pageTierThreeFont,pageHeaderFont;
	private Color pageHeaderGradient[];
	private Color pageHeaderTextColor;
	private Font sectionHeaderFont;
	
//	Map<Configuration,Composite> pageMap;
	
	Composite currentPage;
	
	private ConfigurationSchema getConfigurationSchema() { 
		
		//have we parsed the configuration yet? 
		if(schema == null) { 
			//nope, go do it.
			ConfigurationSchemaParser parser = 
				new ConfigurationSchemaParser();
			
			try {
				schema = parser.parse(getClass().getResourceAsStream(SCHEMA_FILE_NAME));	
			} catch(Throwable t) { 
				System.out.println("Unexpected error, quitting.");
				t.printStackTrace();
				System.exit(-1);
			}
		}
		
		return schema;
	}

	private void formatTree(Tree tree) {
		TreeItem rootItems[] = tree.getItems();	
		for(TreeItem x:rootItems) {
			x.setFont(pageTierOneFont);			
			for(TreeItem y:x.getItems()) {
				y.setFont(pageTierTwoFont);				
				for(TreeItem z:y.getItems()) {
					z.setFont(pageTierThreeFont);
				}
			}
		}	
	}

//	private final static int DEPTH_THRESHOLD = 3;

	private void createPages(Display display,Composite parent,ConfigurationSchema config) { 
		FillLayout parentLayout = new FillLayout();
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.spacing = 0;
		parent.setLayout(parentLayout);

		PaintListener resizer = new PaintListener() {
			public void paintControl(PaintEvent evt) {
				if(currentPage != null) {
					Rectangle r = currentPage.getParent().getBounds();
					currentPage.setBounds(0,0,r.width,r.height);
					currentPage.setVisible(true);
				}
			} 
		};
		
		parent.addPaintListener(resizer);
		for(ConfigurationPage configPage:config.pages) { 
			createPage(display, parent, configPage, 0);			
		}
	}
	
	private void createPage(Display display,Composite parent, ConfigurationPage configPage, int depth) {
		FramedComposite page = new FramedComposite(parent,SWT.SHADOW_IN);
		GridLayout pageGridLayout = new GridLayout(1,true);
		pageGridLayout.marginHeight = pageGridLayout.marginWidth = pageGridLayout.marginLeft = 
			pageGridLayout.marginTop = pageGridLayout.verticalSpacing = 0;
		page.setLayout(pageGridLayout);
		
		//create page header (same label as tree item)
		CLabel pageHeader = new CLabel(page, SWT.NONE);
		pageHeader.setText(configPage.name);
		pageHeader.setFont(pageHeaderFont);
		pageHeader.setBackground( pageHeaderGradient, 
				new int[] { 100 }, true);
		pageHeader.setForeground(pageHeaderTextColor);
		pageHeader.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//create sections
		for(ConfigurationSection s:configPage.sections) { 
			CLabel sectionHeader = new CLabel(page,SWT.SHADOW_OUT);
			sectionHeader.setText(s.name);
			sectionHeader.setFont(sectionHeaderFont);
			sectionHeader.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			sectionHeader.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Composite fields = new Composite(page,SWT.NONE);
			GridLayout fieldsGridLayout = new GridLayout(2,true);
			fieldsGridLayout.marginLeft = 60;
			fields.setLayout(fieldsGridLayout);
			
			for(ConfigurationProperty p:s.properties) { 
				Label fieldLabel = new Label(fields,SWT.NONE);
				fieldLabel.setText(p.label);
				Text textField = new Text(fields,SWT.BORDER);
				textField.setText("Text");				
			}
		}
		
		page.setVisible(false);
		
//		pageMap.put(configPage.name,page);
		configPage.pageWidget = page;
		
		//create sub pages
		for(ConfigurationPage p:configPage.subPages) {
			createPage(display,parent,p,depth+1);
		}
	}
	
//	private void createPageComposite(Display display,Composite parent) { 
//		FillLayout parentLayout = new FillLayout();
//		parentLayout.marginHeight = 0;
//		parentLayout.marginWidth = 0;
//		parentLayout.spacing = 0;
//		parent.setLayout(parentLayout);
//		
//		{
//			Composite page = new Composite(parent,0);
//			GridLayout pageGridLayout = new GridLayout(1,true);
//			pageGridLayout.marginHeight = 0;
//			pageGridLayout.marginWidth = 0;
//			pageGridLayout.marginLeft = 0;
//			pageGridLayout.marginTop = 0;
//			pageGridLayout.verticalSpacing = 0;
//			
////			page.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//			page.setLayout(pageGridLayout);
//			{ //page children
//				CLabel pageHeader = new CLabel(page, 0);
//				pageHeader.setText("Session Initiation Protocol");
//				pageHeader.setFont(pageHeaderFont);
//				pageHeader.setBackground( pageHeaderGradient, 
//						new int[] { 100 }, true);
//				pageHeader.setForeground(pageHeaderTextColor);
//				pageHeader.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			
//				CLabel sectionHeader = new CLabel(page,SWT.SHADOW_OUT);
//				sectionHeader.setText("UDP/IP Settings");
//				sectionHeader.setFont(sectionHeaderFont);
//				sectionHeader.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
//				sectionHeader.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//				
//				Composite fields = new Composite(page,0);
//				GridLayout fieldsGridLayout = new GridLayout(2,true);
//				fieldsGridLayout.marginLeft = 60;
//				fields.setLayout(fieldsGridLayout);
//				{
//					Label fieldLabel = new Label(fields,0);
//					fieldLabel.setText("Label");
//					Text textField = new Text(fields,0);
//					textField.setText("Text");
//					
//					Label fieldLabel2 = new Label(fields,0);
//					fieldLabel2.setText("Label");
//					Text textField2 = new Text(fields,0);
//					textField2.setText("Text");
//				}
//			}
//		}
//	}
	
	private void switchToPage(ConfigurationPage destPage) { 
		if(currentPage != null) { 
			currentPage.setVisible(false);
		}
		
		currentPage = destPage.pageWidget;
		
		Rectangle r = currentPage.getParent().getBounds();
		currentPage.setBounds(0,0,r.width,r.height);
		currentPage.setVisible(true);
	}
	
	ISelectionChangedListener 
		treeSelectListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				ConfigurationPage treeItem =
					(ConfigurationPage)((IStructuredSelection)evt.getSelection()).getFirstElement();
				switchToPage(treeItem); 
			} 
	};
	
	
	@Override
	protected Control createCoolBarControl(Composite parent) {
		CoolBar coolBar = new CoolBar(parent, SWT.NONE);
		
	    ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT);
	    ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
	    toolItem.setText("Save Configuration");
	    toolBar.pack();
	    
	    Point size = toolBar.getSize();
	    CoolItem item = new CoolItem(coolBar, SWT.NONE);
	    item.setControl(toolBar);
	    Point preferred = item.computeSize(size.x, size.y);
	    item.setPreferredSize(preferred);

	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.horizontalSpan = 2;
	    coolBar.setLayoutData(gd);
	    
	    CustomSeparator sep = new CustomSeparator(parent,SWT.HORIZONTAL|SWT.SHADOW_IN);
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.horizontalSpan = 2;
	    sep.setLayoutData(gd);
		
	    return coolBar;
	}

	@Override
	protected Control createContents(Composite parent) {
		Display display = Display.getCurrent();
		
		createCoolBarControl(parent);
		
		createFonts(display);
		createColors(display);
		
		GridLayout gridLayout = new GridLayout(2,false);
		gridLayout.marginWidth = 1;
		gridLayout.marginHeight = 1;
		gridLayout.horizontalSpacing = 5;
		
		parent.setLayout(gridLayout);
		
		FramedComposite treeFrame = new FramedComposite(parent,SWT.SHADOW_IN);
		treeFrame.setLayout(new FillLayout());
		TreeViewer pageTree = new TreeViewer(treeFrame,SWT.FLAT);
		
		GridData treeGridData = new GridData(GridData.FILL_VERTICAL);
		treeGridData.widthHint = 245;
		treeFrame.setLayoutData(treeGridData);

		pageTree.setLabelProvider(new PageTreeLabelProvider());
		pageTree.setContentProvider(new PageTreeContentProvider());
//		pageTree.getTree().setLinesVisible(false);
		pageTree.setInput(getConfigurationSchema());
		pageTree.expandAll();
		
		pageTree.addSelectionChangedListener(treeSelectListener);
		
		formatTree(pageTree.getTree());

		//create composite container
		FramedComposite propertyPages = new FramedComposite(parent,SWT.SHADOW_IN);
//		propertyPages.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		
		GridData pageGridData = new GridData(GridData.FILL_BOTH);
		propertyPages.setLayoutData(pageGridData);
	
//		createPageComposite(display,propertyPages);
		
		createPages(display,propertyPages, schema);
		return pageTree.getTree();
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText("Avian XVR Server Configuration");
		shell.setSize(640,480);
	}

	public void createFonts(Display display) { 
		pageTierOneFont = new Font(display,new FontData[]{new FontData("Arial", 9, SWT.BOLD)});
		pageTierTwoFont = new Font(display,new FontData[]{new FontData("Arial", 9, 0)});
		pageTierThreeFont = new Font(display,new FontData[]{new FontData("Arial", 9, 0)});
		
		pageHeaderFont = new Font(display,new FontData[]{new FontData("Arial",16,SWT.BOLD)});

		sectionHeaderFont = new Font(display,new FontData[]{new FontData("Verdana", 8, SWT.BOLD)});

	}
	
	public void createColors(Display display) { 
		pageHeaderGradient = new Color[2];
			pageHeaderGradient[0] = new Color(display,95,135,233);
			pageHeaderGradient[1] = new Color(display,60,103,234);
			
		pageHeaderTextColor = display.getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application appWindow = new Application();
		
		appWindow.setBlockOnOpen(true);
		appWindow.open();
		
		Display.getCurrent().dispose();
	}

}
