package gibberfish.football.mysql.to.mongo;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import uk.co.mindbadger.footballresults.loader.mapping.FootballResultsMapping;
import uk.co.mindbadger.xml.XMLFileReader;
import uk.co.mindbadger.xml.XMLFileWriter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class FootballMySqlToMongo {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/football";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "sminge1";
	
	
	private Connection conn;
	private MongoClient client;
	private DB mongoDb;
	
	private Map<Integer, String> divisionIdMap = new HashMap<Integer, String> ();
	private Map<Integer, String> teamIdMap = new HashMap<Integer, String> ();
	
	private XMLFileReader reader = new XMLFileReader ();
	private XMLFileWriter writer = new XMLFileWriter ();
	private FootballResultsMapping mapping = new FootballResultsMapping("C:\\Mark\\appConfig\\fra_mapping2.xml", reader, writer);
	
	public FootballMySqlToMongo (String url, String username, String password) throws Exception {
		client = new MongoClient("localhost");			
		mongoDb = client.getDB( "football" );

		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public static void main(String[] args) throws Exception {
		FootballMySqlToMongo converter = new FootballMySqlToMongo (DB_URL, USER, PASS);
		converter.convert ();
		System.out.println("Goodbye!");		
	}

	private void convert() throws SQLException {
		try {
			convertDivisions();
			convertTeams();
			convertSeasonStructure();
			convertFixtures ();
			mapping.saveMappings();
		} finally {
			if (conn != null) conn.close();
		}
	}

	private void convertDivisions() throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
	
			String sql = "SELECT div_id, div_name FROM division";
			ResultSet rs = stmt.executeQuery(sql);
	
			DBCollection divisions = mongoDb.getCollection("division");
			divisions.drop();
				
			while (rs.next()) {
				int divId = rs.getInt("div_id");
				String divIdAsString = (new Integer(divId)).toString();
				
				String divName = rs.getString("div_name");
					
				BasicDBObject division = new BasicDBObject("div_name", divName); 
				divisions.insert(division);
				ObjectId id = (ObjectId)division.get( "_id" );
								
				divisionIdMap.put(divId, id.toString());
				
				mapping.addDivisionMapping("soccerbase", divIdAsString, id.toString());
				
				System.out.println("ID Inserted : " + id);
			}
	
			rs.close();
			stmt.close();
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	private void convertTeams() throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
	
			String sql = "SELECT team_id, team_name FROM team";
			ResultSet rs = stmt.executeQuery(sql);
	
			DBCollection teams = mongoDb.getCollection("team");
			teams.drop();
				
			while (rs.next()) {
				int teamId = rs.getInt("team_id");
				String teamIdAsString = (new Integer(teamId)).toString();
				
				String teamName = rs.getString("team_name");
					
				BasicDBObject team = new BasicDBObject("team_name", teamName); 
				teams.insert(team);
				ObjectId id = (ObjectId)team.get( "_id" );
								
				teamIdMap.put(teamId, id.toString());
				
				mapping.addTeamMapping("soccerbase", teamIdAsString, id.toString());
				
				System.out.println("Team added with id : " + id);
			}
	
			rs.close();
			stmt.close();
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	private void convertSeasonStructure() throws SQLException {
		Statement seasonStatement = null;
		ResultSet seasonResultSet = null;
		PreparedStatement divisionsInSeasonStatement = null;
		ResultSet divisionsInSeasonResultSet = null;
		PreparedStatement teamsInDivisionInSeasonStatement = null;
		ResultSet teamsInDivisionInSeasonResultSet = null;
		
		String seasonSql = "SELECT ssn_num FROM season";
		String divisionsInSeasonSql = "SELECT div_id, div_pos FROM season_division WHERE ssn_num = ?";
		String teamsInDivisionInSeasonSql = "SELECT team_id FROM season_division_team WHERE ssn_num = ? AND div_id = ?";

		DBCollection seasons = mongoDb.getCollection("season");
		seasons.drop();
		
		try {
			seasonStatement = conn.createStatement();
			seasonResultSet = seasonStatement.executeQuery(seasonSql);
					
			while (seasonResultSet.next()) {
				int ssnNum = seasonResultSet.getInt("ssn_num");

				BasicDBObject season = new BasicDBObject("_id", ssnNum); 
				
				
				divisionsInSeasonStatement = conn.prepareStatement(divisionsInSeasonSql);
				divisionsInSeasonStatement.setString(1, Integer.toString(ssnNum));
				
				divisionsInSeasonResultSet = divisionsInSeasonStatement.executeQuery();
				
				
				ArrayList <BasicDBObject> divisionsInSeason = new ArrayList <BasicDBObject> ();
				
				while (divisionsInSeasonResultSet.next()) {
					int divId = divisionsInSeasonResultSet.getInt("div_id");
					int divPos = divisionsInSeasonResultSet.getInt("div_pos");
					
					BasicDBObject seasonDivision = new BasicDBObject("_id", divisionIdMap.get(divId));
					seasonDivision.append("div_pos", divPos);
					
					
					teamsInDivisionInSeasonStatement = conn.prepareStatement(teamsInDivisionInSeasonSql);
					teamsInDivisionInSeasonStatement.setInt(1, ssnNum);
					teamsInDivisionInSeasonStatement.setInt(2, divId);
					
					teamsInDivisionInSeasonResultSet = teamsInDivisionInSeasonStatement.executeQuery();
					
					ArrayList <String> teamsInDivision = new ArrayList<String> ();
					
					while (teamsInDivisionInSeasonResultSet.next()) {
						int teamId = teamsInDivisionInSeasonResultSet.getInt("team_id");
						
						teamsInDivision.add(teamIdMap.get(teamId));
					}
					
					seasonDivision.append("teams", teamsInDivision);
					
					divisionsInSeason.add(seasonDivision);
					
					teamsInDivisionInSeasonResultSet.close();
					teamsInDivisionInSeasonStatement.close();
				}
				
				
				
				
				season.append("divisions", divisionsInSeason);
				
				seasons.insert(season);
								
				
				System.out.println("Season added with id : " + ssnNum);
				
				divisionsInSeasonResultSet.close();
				divisionsInSeasonStatement.close();
			}
	
			seasonResultSet.close();
			seasonStatement.close();
		} finally {
			if (seasonResultSet != null) seasonResultSet.close();
			if (seasonStatement != null) seasonStatement.close();
			if (divisionsInSeasonResultSet != null) divisionsInSeasonResultSet.close();
			if (divisionsInSeasonStatement != null) divisionsInSeasonStatement.close();
			if (teamsInDivisionInSeasonResultSet != null) teamsInDivisionInSeasonResultSet.close();
			if (teamsInDivisionInSeasonStatement != null) teamsInDivisionInSeasonStatement.close();
		}
	}

	private void convertFixtures() throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
	
			String sql = "SELECT ssn_num, home_team_id, away_team_id, fixture_date, div_id, home_goals, away_goals FROM fixture";
			ResultSet rs = stmt.executeQuery(sql);
	
			DBCollection fixtures = mongoDb.getCollection("fixture");
			fixtures.drop();
				
			while (rs.next()) {
				int ssnNum = rs.getInt("ssn_num");
				int homeTeamId = rs.getInt("home_team_id");
				int awayTeamId = rs.getInt("away_team_id");
				Date fixtureDate = rs.getDate("fixture_date");
				int divId = rs.getInt("div_id");
				int homeGoals = rs.getInt("home_goals");
				int awayGoals = rs.getInt("away_goals");
				
					
				BasicDBObject fixture = new BasicDBObject("ssn_num", ssnNum)
					.append("home_team_id", teamIdMap.get(homeTeamId))
					.append("away_team_id", teamIdMap.get(awayTeamId))
					.append("fixture_date", fixtureDate)
					.append("div_id", divisionIdMap.get(divId))
					.append("home_goals", homeGoals)
					.append("away_goals", awayGoals)
					;
				
				fixtures.insert(fixture);
				ObjectId id = (ObjectId)fixture.get( "_id" );
				
				System.out.println("Fixture added with id : " + id);
			}
	
			rs.close();
			stmt.close();
		} finally {
			if (stmt != null) stmt.close();
		}
		
	}

}
