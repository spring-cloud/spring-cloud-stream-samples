package kafka.streams.interactive.query;

import java.util.Objects;

/**
 * @author Soby Chacko
 */
public class SongBean {

	private String artist;
	private String album;
	private String name;

	public SongBean() {}

	public SongBean(final String artist, final String album, final String name) {

		this.artist = artist;
		this.album = album;
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(final String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(final String album) {
		this.album = album;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return "SongBean{" +
				"artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SongBean that = (SongBean) o;
		return Objects.equals(artist, that.artist) &&
				Objects.equals(album, that.album) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, album, name);
	}
}
