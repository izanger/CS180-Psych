/**
 * "View class implements Java GUI classes"
 *
 * Created by Ian on 10/27/2016.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FoilMakerView extends JFrame implements ActionListener {
	public JButton loginButton, registerButton, newGame, joinGame, startGame, jGame, submit, nextRound, submitOption;
	public FoilMakerController controller;
	public FoilMakerModel model;
	public JLabel status = new JLabel("Welcome");
	public JLabel username = new JLabel("UserName");
	public JLabel password = new JLabel("Password");
	public JLabel key = new JLabel("Game Key");
	public JLabel joinKey = new JLabel("Enter Game Key");
	public JLabel yourSugg = new JLabel("Your Suggestion");
	public JLabel description = new JLabel("A Group of Zebras");
	public JLabel whatWord = new JLabel("What is the word for");
	public JLabel optionBelow = new JLabel("Pick Your Option Below");
	public JTextField usernameInput = new JTextField("<Username>"); 
	public JTextField passwordInput = new JTextField("<Password>");
	public JTextField participants, enterKey, suggestionBox, roundResults, overallResults;
	public JLabel gameKey; //call this "in the moment" instead
	public JRadioButton[] options = new JRadioButton[3];
	
	public FoilMakerView(FoilMakerController controller, FoilMakerModel model){
		this.controller = controller;
		this.model = model;
	}
	
	public void setUpView(){
		loginScreen();
		
	}
	
	public void loginScreen(){
		Container login;
		this.setSize(200, 200);
		this.setVisible(true);
		login = this.getContentPane();
		login.setLayout(new BorderLayout());
		login.add(status, BorderLayout.SOUTH);
		JPanel userPanel = new JPanel(new FlowLayout());
		userPanel.add(username);
		userPanel.add(usernameInput);
		login.add(userPanel, BorderLayout.CENTER);
		JPanel passPanel = new JPanel(new FlowLayout());
		passPanel.add(password);
		passPanel.add(passwordInput);
		login.add(passPanel, BorderLayout.CENTER);
		loginButton = new JButton("Login");
		registerButton = new JButton("Register");
		login.add(loginButton, BorderLayout.SOUTH);
		login.add(registerButton, BorderLayout.SOUTH);
	}
	
	public void gameSelection(){
		Container game;
		this.setSize(200, 200);
		game = this.getContentPane();
		game.setLayout(new BorderLayout());
		newGame = new JButton("Start New Game");
		joinGame = new JButton("Join A Game");
		game.add(newGame, BorderLayout.CENTER);
		game.add(joinGame, BorderLayout.CENTER);
	}
	
	public void startNewGame(){
		Container start;
		this.setSize(200, 200);
		start = this.getContentPane();
		start.setLayout(new BorderLayout());
		start.add(key, BorderLayout.NORTH);
		gameKey = new JLabel(model.gameToken);
		start.add(gameKey, BorderLayout.NORTH);
		start.add(participants, BorderLayout.CENTER);
		startGame = new JButton("Start Game");
		start.add(startGame, BorderLayout.SOUTH);
		
	}
	
	public void joinAGame(){
		Container join;
		this.setSize(200, 200);
		join = this.getContentPane();
		join.setLayout(new BorderLayout());
		join.add(joinKey, BorderLayout.NORTH);
		join.add(enterKey, BorderLayout.CENTER);
		join.add(jGame, BorderLayout.SOUTH);
	}
	
	public void waitForLeader(){
		Container wait;
		this.setSize(200, 200);
		wait = this.getContentPane();
		wait.setLayout(new BorderLayout());
		JLabel waiting = new JLabel("Waiting for Leader...");
		wait.add(waiting, BorderLayout.CENTER);
	}
	
	public void launchGame(){
		Container word;
		this.setSize(200, 200);
		word = this.getContentPane();
		word.setLayout(new BorderLayout());
		JPanel question = new JPanel();
		question.setLayout(new FlowLayout());
		question.add(whatWord, BorderLayout.NORTH);
		question.add(description, FlowLayout.CENTER);
		JPanel suggestion = new JPanel();
		suggestion.setLayout(new FlowLayout());
		suggestion.add(yourSugg, BorderLayout.NORTH);
		suggestion.add(suggestionBox);
		submit = new JButton("Submit Suggestion");
		word.add(question, BorderLayout.NORTH);
		word.add(suggestion, BorderLayout.CENTER);
		word.add(submit, BorderLayout.SOUTH);
	}
	
	public void pickOption(){
		Container optionCon;
		this.setSize(200, 200);
		optionCon = this.getContentPane();
		optionCon.setLayout(new BorderLayout());
		optionCon.add(optionBelow, BorderLayout.NORTH);
		ButtonGroup group = new ButtonGroup();
		options[0] = new JRadioButton(model.possibleAnswer1);
		options[1] = new JRadioButton(model.possibleAnswer2);
		options[2] = new JRadioButton(model.possibleAnswer3);
		submitOption = new JButton("Submit Option");
		optionCon.add(options[0], BorderLayout.CENTER);
		optionCon.add(options[1], BorderLayout.CENTER);
		optionCon.add(options[2], BorderLayout.CENTER);
		optionCon.add(submitOption, BorderLayout.SOUTH);		
	}
	
	public void gameResults(){
		Container results;
		this.setSize(200, 200);
		results = this.getContentPane();
		results.setLayout(new BorderLayout());
		JPanel round = new JPanel();
		round.setBorder(BorderFactory.createTitledBorder("Round Result"));
		JPanel overall = new JPanel();
		overall.setBorder(BorderFactory.createTitledBorder("Overall Results"));
		nextRound = new JButton("Next Round");
		results.add(round, BorderLayout.NORTH);
		results.add(overall, BorderLayout.CENTER);
		results.add(nextRound, BorderLayout.SOUTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton pressed = (JButton) e.getSource();
		String name = usernameInput.getText();
		String pass = passwordInput.getText();
		if(pressed==loginButton){
			controller.login(name, pass);
			gameSelection();
		}
		else if(pressed==registerButton){
			controller.createNewUser(name, pass);
			gameSelection();
		}
		else if(pressed==newGame){
			startNewGame();
			controller.startNewGame(model.userToken);
		}
		else if(pressed==joinGame){
			joinAGame();
			String enkey = enterKey.getText();
			controller.joinGame(model.userToken, enkey);
		}
		else if(pressed==startGame){
			launchGame();
		}
		else if(pressed==jGame){
			waitForLeader();
		}
		else if(pressed==submit){
			String newSuggestion = suggestionBox.getText();
			controller.checkForRoundOptions();
			
		}
		else if(pressed==nextRound){
			
		}
	}
	
}
