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
	public JLabel description;
	public JLabel whatWord = new JLabel("What is the word for");
	public JLabel optionBelow = new JLabel("Pick Your Option Below");
	public JTextField usernameInput = new JTextField("<Username>"); 
	public JTextField passwordInput = new JTextField("<Password>");
	public JTextField participants;
	public JTextField enterKey = new JTextField("<Enter Game Key>");
	public JTextField suggestionBox = new JTextField("<Enter Suggestion>");
	public JTextField roundResults = new JTextField("");
	public JTextField overallResults = new JTextField("");
	public JLabel gameKey;
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
		this.setSize(500, 500);
		this.setVisible(true);
		login = this.getContentPane();
		login.setLayout(new FlowLayout());
		login.add(status, BorderLayout.SOUTH);
		JPanel userPanel = new JPanel(new FlowLayout());
		userPanel.add(username);
		userPanel.add(usernameInput);
		login.add(userPanel);
		JPanel passPanel = new JPanel(new FlowLayout());
		passPanel.add(password);
		passPanel.add(passwordInput);
		login.add(passPanel);
		loginButton = new JButton("Login");
		registerButton = new JButton("Register");
		login.add(loginButton);
		loginButton.addActionListener(this);
		login.add(registerButton);
		loginButton.addActionListener(this);
	}
	
	public void gameSelection(){
		Container game;
		this.setSize(500, 500);
		this.setVisible(true);
		game = this.getContentPane();
		game.setLayout(new FlowLayout());
		newGame = new JButton("Start New Game");
		joinGame = new JButton("Join A Game");
		game.add(newGame);
		newGame.addActionListener(this);
		game.add(joinGame);
		joinGame.addActionListener(this);
	}
	
	public void startNewGame(){
		Container start;
		this.setSize(500, 500);
		this.setVisible(true);
		start = this.getContentPane();
		start.setLayout(new FlowLayout());
		start.add(key);
		gameKey = new JLabel(model.gameToken);
		start.add(gameKey);
		start.add(participants);
		startGame = new JButton("Start Game");
		start.add(startGame);
		startGame.addActionListener(this);
		
	}
	
	public void joinAGame(){
		Container join;
		this.setSize(500, 500);
		this.setVisible(true);
		join = this.getContentPane();
		join.setLayout(new FlowLayout());
		join.add(joinKey);
		join.add(enterKey);
		jGame = new JButton("Join Game");
		join.add(jGame);
		jGame.addActionListener(this);
	}
	
	public void waitForLeader(){
		Container wait;
		this.setSize(1000, 1000);
		this.setVisible(true);
		wait = this.getContentPane();
		wait.setLayout(new FlowLayout());
		JLabel waiting = new JLabel("Waiting for Leader...");
		wait.add(waiting);
	}
	
	public void launchGame(){
		Container word;
		this.setSize(1000, 1000);
		this.setVisible(true);
		word = this.getContentPane();
		word.setLayout(new FlowLayout());
		JPanel question = new JPanel();
		question.setLayout(new FlowLayout());
		question.add(whatWord);
		question.add(description);
		JPanel suggestion = new JPanel();
		suggestion.setLayout(new FlowLayout());
		suggestion.add(yourSugg);
		suggestion.add(suggestionBox);
		submit = new JButton("Submit Suggestion");
		word.add(question);
		word.add(suggestion);
		word.add(submit);
		submit.addActionListener(this);
	}
	
	public void pickOption(){
		Container optionCon;
		this.setSize(1000, 1000);
		this.setVisible(true);
		optionCon = this.getContentPane();
		optionCon.setLayout(new FlowLayout());
		optionCon.add(optionBelow);
		ButtonGroup group = new ButtonGroup();
		submitOption = new JButton("Submit Option");
		options[0].addActionListener(this);
		options[1].addActionListener(this);
		options[2].addActionListener(this);
		group.add(options[0]);
		group.add(options[1]);
		group.add(options[2]);
		optionCon.add(options[0]);
		optionCon.add(options[1]);
		optionCon.add(options[2]);
		optionCon.add(submitOption);		
	}
	
	public void gameResults(){
		Container results;
		this.setSize(1000, 1000);
		this.setVisible(true);
		results = this.getContentPane();
		results.setLayout(new FlowLayout());
		JPanel round = new JPanel();
		round.setBorder(BorderFactory.createTitledBorder("Round Result"));
		JPanel overall = new JPanel();
		overall.setBorder(BorderFactory.createTitledBorder("Overall Results"));
		nextRound = new JButton("Next Round");
		results.add(round);
		results.add(overall);
		results.add(nextRound);
		nextRound.addActionListener(this);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		JButton pressed = (JButton) e.getSource();
		if(pressed==loginButton){
			controller.login(model.username, model.password);
			gameSelection();
		}
		else if(pressed==registerButton){
			controller.createNewUser(model.username, model.password);
			gameSelection();
		}
		else if(pressed==newGame){
			participants = new JTextField(model.username);
			participants.setBorder(BorderFactory.createTitledBorder("Participants"));
			controller.startNewGame(model.userToken);
			startNewGame();
			
		}
		else if(pressed==joinGame){
			participants = new JTextField(model.username + "\n" + model.player2Username);
			String enkey = enterKey.getText();
			controller.joinGame(model.userToken, enkey);
			joinAGame();
		}
		else if(pressed==startGame){
			while(model.currentNumberOfPlayers<2){
				controller.checkForNewPlayer();
			}
			controller.allParticipantsHaveJoined(model.userToken, model.gameToken);
			controller.checkForStatusOfNextRound();
			description = new JLabel(model.currentQuestion);
			launchGame();
		}
		else if(pressed==jGame){
			waitForLeader();
			controller.allParticipantsHaveJoined(model.userToken, model.gameToken);
			controller.checkForStatusOfNextRound();
			description = new JLabel(model.currentQuestion);
			launchGame();
		}
		else if(pressed==submit){
			String newSuggestion = suggestionBox.getText();
			controller.playerSuggestion(model.userToken, model.gameToken, newSuggestion);
			controller.checkForRoundOptions();
			options[0] = new JRadioButton(model.possibleAnswer1);
			options[1] = new JRadioButton(model.possibleAnswer2);
			options[2] = new JRadioButton(model.possibleAnswer3);
			pickOption();
		}
		else if(pressed==submitOption){
			if(options[0].isSelected())
				controller.playerChoice(model.userToken, model.gameToken, model.possibleAnswer1);
			if(options[1].isSelected())
				controller.playerChoice(model.userToken, model.gameToken, model.possibleAnswer2);
			if(options[2].isSelected())
				controller.playerChoice(model.userToken, model.gameToken, model.possibleAnswer3);
			controller.checkForRoundResult();
			gameResults();
		}
		else if(pressed==nextRound){
			if(model.isGameOver==true){
				controller.logOut();
			}
			controller.checkForStatusOfNextRound();
			description = new JLabel(model.currentQuestion);
			launchGame();
		}
	}
	
}
