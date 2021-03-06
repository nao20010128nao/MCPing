package com.nao20010128nao.MCPing.pc;

import java.util.List;

/**
 * Query reply for MC 1.9~
 */
public class Reply19 implements PCQueryResult {

	public Description description;
	public Players players;
	public Version version;
	public String favicon;

	/**
	 * @return the MOTD
	 */
	public Description getDescription() {
		return this.description;
	}

	/**
	 * @return @{link Players}
	 */
	public Players getPlayers() {
		return this.players;
	}

	/**
	 * @return @{link Version}
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * @return Base64 encoded favicon image
	 */
	public String getFavicon() {
		return this.favicon;
	}

	public class Players {
		public int max;
		public int online;
		public List<Player> sample;

		/**
		 * @return Maximum player count
		 */
		public int getMax() {
			return this.max;
		}

		/**
		 * @return Online player count
		 */
		public int getOnline() {
			return this.online;
		}

		/**
		 * @return List of some players (if any) specified by server
		 */
		public List<Player> getSample() {
			return this.sample;
		}
	}

	public class Player {
		public String name;
		public String id;

		/**
		 * @return Name of player
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return Unknown
		 */
		public String getId() {
			return this.id;
		}

	}

	public class Version {
		public String name;
		public int protocol;

		/**
		 * @return Version name (ex: 13w41a)
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return Protocol version
		 */
		public int getProtocol() {
			return this.protocol;
		}
	}

	public class Description {
		public String text;
	}
}
