import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPFileFilter;

class CMCQTestGridBag
{
	public static void main(String args[])
	{
      new InitialForm();		
	}
}

class Test implements ActionListener
{
 String questionAndOption[][] = new String[100][5];
 int chosenAnswer[] = new int[100];
 int correctAnswer[] = new int[100];
 int currentQuestion = 0;
 boolean attemptedQuestion[] = new boolean[100];
 int noOfQuestions = 0;
 int marks = 0;
 int totalTime = 10;
 JLabel timerLabel;
 JLabel question;
 ButtonGroup optionGroup; 
 JRadioButton option[] = new JRadioButton[4];
 JButton startTest,stopTest,nextButton,viewCorrectAnswers,viewOthersResult ; 
 WindowListener wndCloser;
 Timer timer;
 JFrame f;
 String ip,name;
 Font bigArialFont;
 void loadQuestionsAndOptions()
 {
	String csvFile = "questions.txt";
	String line = "";
	BufferedReader br = null;
	String cvsSplitBy="\",\"";
	try	
	{
	   br = new BufferedReader(new FileReader(csvFile));
	   while(( line = br.readLine()) != null)	
	   {
	  	String[] lineArr = line.split(cvsSplitBy);
	  	for(int i = 0; i < 5; i++)
	  		questionAndOption[noOfQuestions][i] = lineArr[i].replace("\"",""); 
	  	correctAnswer[noOfQuestions] = Integer.parseInt(lineArr[5].replace("\"",""));	
	    noOfQuestions += 1;
	   }
	 }
	 catch(Exception e)
	 {
	 	e.printStackTrace();
	 }
	 finally
	 {
	  try
	  {
	  	br.close();
	  }
	  catch(Exception e)
	  {
	  	e.printStackTrace();
	  }
	 }
 }

 Test(String name,String ip)
 {
 	this.ip = ip;
 	this.name = name;
 	
	loadQuestionsAndOptions();
 	bigArialFont = new Font("Arial",Font.PLAIN,20);	
    f = new JFrame(" C MCQ TEST ");
	f.setFont(bigArialFont);
	f.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	wndCloser = new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				InteractWithFTPServer dc = new InteractWithFTPServer(ip);
				dc.downloadCountFile();
				dc.changeCountFile("decrement");
				dc.uploadFile("count.txt");		
		}
	};
     f.addWindowListener(wndCloser);

	startTest = new JButton("Start Test");
	startTest.addActionListener(this);
	c.ipady = 20;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 0;
	c.gridy = 0;
	c.insets = new Insets(20,20,50,20);
	f.add(startTest,c);

	
	
	timerLabel = new JLabel("20:00",SwingConstants.CENTER);
	c.ipady = 20;
	timerLabel.setFont(bigArialFont);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 1;
	c.gridy = 0;
	c.insets = new Insets(20,20,50,20);
	f.add(timerLabel,c);

	question = new JLabel();
	question.setFont(bigArialFont);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridwidth=3;
	c.gridx=0;
	c.gridy=1;
	f.add(question,c);
	
    optionGroup = new ButtonGroup();
    int count = 0;
    for(int i=0;i<2;i++)
    {
    	for(int j=0;j<2;j++)
    	{
			option[count] = new JRadioButton();
			option[count].setFont(bigArialFont);
	    	optionGroup.add(option[count]);	
	  		c.fill = GridBagConstraints.BOTH;
			c.gridwidth=1;
			c.gridx=j;
			c.gridy=i+2;
			f.add(option[count],c);
			count += 1;
		}
	}

	nextButton= new JButton("Next Question");
	nextButton.addActionListener(this);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridwidth=2;
	c.gridx=0;
	c.gridy=4;
	c.ipady = 20;
	f.add(nextButton,c);
	
	JLabel anchorLabel = new JLabel("");
	c.weighty =1.0;
	c.gridx=0;
	c.gridy=50;
	c.gridwidth=1;
	f.add(anchorLabel,c);
	timer = new Timer(1000,new TimerHandler(this));
	setRadioButtonState(false);
	displayQuestionAndOption();
	f.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	f.setSize(1000,600);
	f.setVisible(true);
 }
 
 void displayQuestionAndOption()
 {
 	
	question.setText( (currentQuestion+1) + ". " + questionAndOption[currentQuestion][0]);
 	for(int i = 0; i < 4; i++)	
 			option[i].setText(questionAndOption[currentQuestion][i+1]);
 }
 public void actionPerformed(ActionEvent ae)
 {
	if(ae.getSource() == nextButton)
	{
	 	int index = 999;
		 for(int i = 0; i < 4; i++)
		 {
		  if(option[i].isSelected())
		  {
		  	optionGroup.clearSelection();
		  	index = i+1;
		  	attemptedQuestion[currentQuestion] = true;
		  	break;
		  }	  
		 }
		 if(index == 999)
		 {
		   JOptionPane.showMessageDialog(f,"Please select an Answer.","No Answer Selected",JOptionPane.ERROR_MESSAGE);
			return;
		 }
		chosenAnswer[currentQuestion] = index;
		if(index == correctAnswer[currentQuestion])
			marks += 1;
		if(currentQuestion == noOfQuestions-1)
		{
			setRadioButtonState(false);
			nextButton.setText("Please wait till test ends");
			nextButton.setEnabled(false);
			return;
		}
		currentQuestion += 1;
		displayQuestionAndOption();	
	}
	else if (ae.getSource() == startTest)
	{
		startTest.setEnabled(false);
		setRadioButtonState(true);
		JOptionPane.showMessageDialog(f,"Test now Started");
		timer.start();
	}
	else if(ae.getSource() == viewCorrectAnswers)
	{
		displayCorrectAnswers();
	}
	else if(ae.getSource() == viewOthersResult)
	{
		InteractWithFTPServer gc = new InteractWithFTPServer(ip);
		gc.downloadCountFile();
		try	
		{
			String line = "";
	   		BufferedReader br = new BufferedReader(new FileReader("count.txt"));
	   		while(( line = br.readLine()) != null)	
	   		{
				if(Integer.parseInt(line) > 0)
					JOptionPane.showMessageDialog(f,"Some People have Still Not Completed the Test. Please Try Again after a While");
				else
					displayOthersResult();	
	  		 }
	  		 br.close();
	 	}
	 	catch(Exception e)
	 	{
	 		e.printStackTrace();
	 	}
	}	 
 }
 
 void setRadioButtonState(boolean state)
 {
 	for(int i = 0; i < 4; i++)
 	{
 		option[i].setEnabled(state);
 	}
 }
 
 void displayResult()
 {
 	f.setLayout(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	JLabel marksLabel = new JLabel("You have scored " + marks + "/" + noOfQuestions,JLabel.CENTER);
 	marksLabel.setFont(new Font(bigArialFont.getFontName(),Font.BOLD, 32));
 	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 0;
	c.gridy = 0;
	c.ipady = 50;
	c.ipadx = 50;
	c.insets = new Insets(55,55,55,55);
 	f.add(marksLabel,c);
 	
 	viewCorrectAnswers = new JButton("View Correct Answers");
 	viewCorrectAnswers.addActionListener(this);
 	c.gridy = 1;
 	f.add(viewCorrectAnswers,c);
 	
 	viewOthersResult = new JButton("View Others Result");
 	viewOthersResult.addActionListener(this);
 	c.gridy = 2;
 	f.add(viewOthersResult,c);
 	f.removeWindowListener(wndCloser);
 	sendResultToServer();
 	f.repaint();
 	f.revalidate();
 }
 
 void displayCorrectAnswers()
 {
 	JFrame ansFrame = new JFrame("View Correct Answers");
 	JPanel container = new JPanel();
 	container.setLayout(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	int count=0;
 	for(int i = 0; i < noOfQuestions; i++)
 	{
 		JLabel q = new JLabel((i+1) + ". " + questionAndOption[i][0]);
 		q.setFont(bigArialFont);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx=0.5;
 		c.gridx = 0;
 		c.gridy = count++;
 		c.ipady = 10;
 		c.insets = new Insets(5,5,5,5);
 		container.add(q,c);
 		String chosenAns = questionAndOption[i][chosenAnswer[i]];
 		String correctAns = questionAndOption[i][correctAnswer[i]];
 		String ans = "";
 		int insetLeft = f.getFontMetrics(bigArialFont).stringWidth((i+1) + ".  ");
 		if(attemptedQuestion[i])
 		{
 			ans = "Your answer was - " + chosenAns + ".";
	 		JLabel chosenAnsLabel = new JLabel(ans);
	 		chosenAnsLabel.setFont(bigArialFont);
	 		c.gridy = count++;
	 		if(chosenAns == correctAns)
	 		{
		 		chosenAnsLabel.setForeground(Color.GREEN);
		 		c.insets = new Insets(5,insetLeft,40,5);
		 		container.add(chosenAnsLabel,c);	
		 		continue;
		 	}
		 	c.insets = new Insets(5,insetLeft,5,5);
		 	chosenAnsLabel.setForeground(Color.RED);
	 		container.add(chosenAnsLabel,c);

 		}
 		
	 	ans =  "Correct Answer is - " + correctAns + ".";
 		JLabel ansLabel = new JLabel(ans);
 		ansLabel.setFont(bigArialFont);
 		ansLabel.setForeground(Color.BLUE);
 		c.gridy = count++;
 		c.insets = new Insets(5,insetLeft,40,5);
 		container.add(ansLabel,c);
 	}
 	JLabel anchorLabel = new JLabel("");
 	c.weighty=1.0;;
 	c.gridx = 0;
 	c.gridy = count++;
 	c.ipadx = 20;
 	c.ipady = 20;
 	container.add(anchorLabel,c);
 	
 	JScrollPane jsp = new JScrollPane(container);
 	ansFrame.setLayout(new BorderLayout());
 	ansFrame.add(jsp);
 	ansFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	ansFrame.setSize(1000,600);
	ansFrame.setVisible(true);
 	 }
 
 	void displayOthersResult()
 	{
 		String name[] = new String[100];
 		double marks[] = new double[100];
 		int count[] = new int[1];
 		InteractWithFTPServer gr = new InteractWithFTPServer(ip);
 		gr.getResult(name,marks,count);
		JFrame resultFrame= new JFrame("View Complete Result");
		resultFrame.setSize(1000,600);
		resultFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		resultFrame.getContentPane().add(new ChartPanel(marks, name, "MCQ Test Result",count[0]));
		resultFrame.setVisible(true);
		 
 	}
	void sendResultToServer()
	{
		InteractWithFTPServer sr = new InteractWithFTPServer(ip);
		sr.sendResult(name,marks); 
	}
}

class TimerHandler implements ActionListener
{
	Test t;
	TimerHandler(Test t)
	{
		this.t = t;
	}
	@Override
	public void actionPerformed(ActionEvent ae)
	{
	if(t.totalTime == 0)
	{
		t.timer.removeActionListener(this);
		t.f.getContentPane().removeAll();		
		t.displayResult();
		return;
	}
	String timeString="";
	int minute = t.totalTime / 60;
	int seconds = t.totalTime % 60;
	if(minute < 10)
	 timeString = "0" + minute +":"+seconds;
	if(seconds < 10)
	 timeString = minute + ":" + "0" + seconds;
	else
	 timeString = minute + ":" + seconds; 
	t.timerLabel.setText(timeString);
	t.totalTime--;
	}
}

class InitialForm implements ActionListener
{
	String name;
	String ip;
    JFrame f;
    Font bigArialFont;
    JButton goToTest;
    JTextField nameTextField,ipTextField;
    
    InitialForm()
    {
    	f = new JFrame("C MCQ Test");
		WindowListener wndCloser = new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
		};
     	f.addWindowListener(wndCloser);
    	bigArialFont = new Font("Arial",Font.BOLD,25);
    	Font moderateArialFont = new Font("Arial",Font.BOLD,20);
    	f.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	
    	c.fill = GridBagConstraints.HORIZONTAL;
 		c.ipadx = 50;
 		c.ipady = 50;
 		c.insets = new Insets(25,25,125,25);
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		
 		JLabel mainLabel = new JLabel("C MCQ TEST",JLabel.CENTER);
 		mainLabel.setFont(bigArialFont);
 		f.add(mainLabel,c);
 		
 		
 		c.ipadx = 150;
 		c.ipady = 25;
 		c.insets = new Insets(25,25,25,25);
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 1;
 		JLabel nameLabel = new JLabel("Enter Name");
 		nameLabel.setFont(moderateArialFont);
  		f.add(nameLabel,c);
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		nameTextField = new JTextField();
 		nameTextField.setFont(new Font("Arial",Font.PLAIN,16));
 		f.add(nameTextField,c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
		JLabel ipLabel = new JLabel("Server IP");
		ipLabel.setFont(moderateArialFont);
 		f.add(ipLabel,c);
 		
 		c.gridx = 1;
 		c.gridy = 2;
 		ipTextField = new JTextField("localhost");
 		ipTextField.setFont(new Font("Arial",Font.PLAIN,16));
 		f.add(ipTextField,c);
 		   	
    	c.gridx = 0;
    	c.gridy = 3;
    	c.gridwidth=2;
    	c.ipady = 40;
    	c.ipadx = 40;
    	goToTest = new JButton("Go To Test");
    	goToTest.addActionListener(this);
		f.add(goToTest,c);
		f.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		f.setSize(1000,600);
		f.setVisible(true);

    }
    
 	public void actionPerformed(ActionEvent ae)
 	{
 		if(ae.getSource() == goToTest)
 		{
 			name = nameTextField.getText();
 			ip = ipTextField.getText();
 			if(name.length() == 0 || ip.length() == 0)
 			{
 				JOptionPane.showMessageDialog(f, "One or More Field is Empty. Please Fill in Correctly and Then Proceed","Invalid Field",JOptionPane.ERROR_MESSAGE);
 				return;
 			}		
 			else
 			{
 				getQuestionsFromServer();
 			//	f.setVisible(false);
 				new Test(name,ip);
 			}
 		}
 	}
 	
 	void getQuestionsFromServer()
 	{
 		InteractWithFTPServer dq = new InteractWithFTPServer(ip);
 		dq.downloadQuestionFile();
 	}	   
}

class InteractWithFTPServer
{
	String ip;
	FTPClient ftp;
	
	InteractWithFTPServer(String ip)
	{
		this.ip = ip;
	}
	void connectToFTPServer()
	{
		try
		{
			String serverAddress = ip;
			String userId = "pi";
			String password = "raspberry";
			String remoteDirectory = "/";
			String localDirectory = "/home/pi";

			ftp = new FTPClient();
			ftp.connect(serverAddress);
			if(!ftp.login(userId, password))
			{
				ftp.logout();
				return;
			}
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftp.disconnect();
				return;
			}
			ftp.enterLocalPassiveMode();
			while(true)
			{
				if(!lockIsPresent())
					break;
				Thread.sleep(1000);
			}
			acquireLock();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	boolean lockIsPresent() throws IOException 
	{
  		String[] fileNames = ftp.listNames();
  		for(int i = 0; i < fileNames.length; i++)
  		{
  			if(fileNames[i].equals("lock"))
  				return true;
  		}
  		return false;
    }
	
	void acquireLock() throws IOException
	{
		try
		{
			File file = new File("lock");
			file.createNewFile();
			InputStream input;	
			input = new FileInputStream("lock");
			ftp.storeFile("lock", input);
			file.delete();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void disconnectFromFTPServer()
	{
		try
		{
			ftp.deleteFile("lock");
			ftp.logout();
			ftp.disconnect();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}		
	
	void downloadFile(String file)
	{
		try
		{
			connectToFTPServer();
            OutputStream output;
            output = new FileOutputStream("." + File.separator + file);
            ftp.retrieveFile(file, output);
            output.close();
            disconnectFromFTPServer();
       	}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}
	
	void downloadQuestionFile()
	{
		downloadFile("questions.txt");
		downloadCountFile();
		changeCountFile("increment");
		uploadFile("count.txt");		
	}
	
	void downloadCountFile()
	{
		downloadFile("count.txt");
	}
	
	void getResult(String[] name,double[] marks,int count[])
	{	
		count[0] = 0;
		try 
		{
		connectToFTPServer();
		FTPFile[] ftpFiles = ftp.listFiles("",new FTPFileFilter(){
			@Override
			public boolean accept(FTPFile f)
			{
				return f.getName().endsWith("-Result");
			}
		});
		for(int i = 0; i < ftpFiles.length; i++)
		{
			FTPFile file = ftpFiles[i];
			if(!file.isFile()){
				continue;
			}
			String fileName[] = file.getName().split("-");
			name[i] = fileName[0];
			marks[i] = Double.parseDouble(fileName[1]);
			count[0] += 1;
		}
		disconnectFromFTPServer();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void uploadFile(String fileName)
	{
		try
		{
			connectToFTPServer();
			InputStream input;	
			input = new FileInputStream(fileName);
			ftp.storeFile(fileName, input);
			disconnectFromFTPServer();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	void sendResult(String name,int marks)
	{
		String fileName = name + "-" + marks + "-Result";
		try
		{
			File file = new File(fileName);
			file.createNewFile();
			uploadFile(fileName);
			
			downloadCountFile();
			changeCountFile("decrement");
			uploadFile("count.txt");
			
			file.delete();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
	   	}	
	}
	void changeCountFile(String change)
	{
		BufferedWriter out = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader("count.txt"));
			String storedCount;
			int storedCountNumber = 0;
			while ((storedCount = br.readLine()) != null) {
				storedCountNumber=(Integer.parseInt(storedCount==null?"0":storedCount));
			}
			out = new BufferedWriter(new FileWriter("count.txt", false));
			storedCountNumber = (change == "increment") ? storedCountNumber+1 : storedCountNumber -1;
			if(storedCountNumber < 0)
				storedCountNumber = 0;
			storedCount = String.valueOf(storedCountNumber);
			out.write(storedCount);
			out.close();
			br.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}








class ChartPanel extends JPanel {
  private double[] values;

  private String[] names;

  private String title;
  int count;
  public ChartPanel(double[] v, String[] n, String t,int c) {
    names = n;
    values = v;
    title = t;
    count = c;
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (values == null || count == 0)
      return;
    double minValue = 0;
    double maxValue = 0;
    for (int i = 0; i < count; i++) {
      if (minValue > values[i])
        minValue = values[i];
      if (maxValue < values[i])
        maxValue = values[i];
    }

    Dimension d = getSize();
    int clientWidth = d.width;
    int clientHeight = d.height;
    int barWidth = clientWidth / count;

    Font titleFont = new Font("SansSerif", Font.BOLD, 20);
    FontMetrics titleFontMetrics = g.getFontMetrics(titleFont);
    Font labelFont = new Font("SansSerif", Font.BOLD, 16);
    FontMetrics labelFontMetrics = g.getFontMetrics(labelFont);

    int titleWidth = titleFontMetrics.stringWidth(title);
    int y = titleFontMetrics.getAscent();
    int x = (clientWidth - titleWidth) / 2;
    g.setFont(titleFont);
    g.drawString(title, x, y);

    int top = titleFontMetrics.getHeight();
    int bottom = labelFontMetrics.getHeight();
    if (maxValue == minValue)
      return;
    double scale = (clientHeight - top - bottom) / (maxValue - minValue);
    y = clientHeight - labelFontMetrics.getDescent();
    g.setFont(labelFont);

    for (int i = 0; i < count; i++) {
      int valueX = i * barWidth + 1;
      int valueY = top;
      int height = (int) (values[i] * scale);
      if (values[i] >= 0)
        valueY += (int) ((maxValue - values[i]) * scale);
      else {
        valueY += (int) (maxValue * scale);
        height = -height;
      }
      g.setColor(Color.red);
      g.fillRect(valueX, valueY, barWidth - 6, height);
      g.setColor(Color.black);
      g.drawRect(valueX, valueY, barWidth - 6, height);
      int labelWidth = labelFontMetrics.stringWidth(names[i]);
      x = i * barWidth + (barWidth - labelWidth) / 2;
      g.drawString(names[i], x, y);
      int marksWidth = labelFontMetrics.stringWidth(String.valueOf((int) values[i]));
      g.drawString(String.valueOf((int) values[i]),x + (labelWidth-marksWidth)/2 ,valueY);
    }
  }
}


