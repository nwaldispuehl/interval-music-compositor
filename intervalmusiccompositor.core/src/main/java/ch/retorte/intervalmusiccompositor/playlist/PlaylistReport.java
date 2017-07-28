package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.CROSS;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;

/**
 * @author nw
 */
public class PlaylistReport {

  private MessageFormatBundle bundle = getBundle("core_imc");

  private FormatTime formatTime = new FormatTime();

  private static final String ITEM_DELIMITER = ", ";
  private static final String RANGE_DELIMITER = "-";
  private static final String EOL_DELIMITER = "\r\n";
  private static final String HORIZONTAL_ROW = "================================================================================";

  private ApplicationData applicationData;

  public PlaylistReport(ApplicationData applicationData) {
    this.applicationData = applicationData;
  }

  public String generateReportFor(Playlist playlist, List<IAudioFile> badTunesList) {

    StringBuilder result = new StringBuilder();

    appendTitle(result);

    int totalPositionSeconds = 0;
    int i = 0;
    for (PlaylistItem playlistItem : playlist) {

      int currentPositionsSeconds = totalPositionSeconds;
      if (playlist.getBlendMode() == CROSS) {
        currentPositionsSeconds += (int) (playlist.getBlendTime() / 2);
      }
      appendMusicLine(result, playlist, i, currentPositionsSeconds, playlistItem);

      totalPositionSeconds += playlistItem.getExtractDurationInSeconds();
      if (playlist.getBlendMode() == CROSS && !playlistItem.isSilentBreak()) {
        totalPositionSeconds -= playlist.getBlendTime();
      }

      if (!(playlistItem instanceof BreakPlaylistItem)) {
        i++;
      }
    }

    appendSoundEffectsList(result, playlist);
    appendBadTunesList(result, badTunesList);

    return result.toString();
  }

  private void appendTitle(StringBuilder builder) {

    String titleString = applicationData.getProgramName();
    String websiteString = bundle.getString("web.website.url");
    String generatedOnString = bundle.getString("imc.playlist.generated_on");
    String playlistString = bundle.getString("imc.playlist.playlist_header");
    String bpmNotReliableLegendString = bundle.getString("imc.playlist.bpm_not_reliable_legend");

    builder.append(titleString);
    builder.append(EOL_DELIMITER);

    builder.append(websiteString);
    builder.append(EOL_DELIMITER);

    builder.append(generatedOnString);
    builder.append(" ");
    builder.append(getFormattedCurrentTime());
    builder.append(EOL_DELIMITER);
    builder.append(EOL_DELIMITER);

    builder.append(playlistString);
    builder.append(" (");
    builder.append(bpmNotReliableLegendString);
    builder.append(")");
    builder.append(EOL_DELIMITER);

    builder.append(HORIZONTAL_ROW);
    builder.append(EOL_DELIMITER);
  }

  private String getFormattedCurrentTime() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getDefault());
    return dateFormat.format(new Date());
  }

  private void appendMusicLine(StringBuilder builder, Playlist playlist, int listPosition, int totalDurationSoFar, PlaylistItem playlistItem) {

    String durationString = bundle.getString("imc.playlist.duration");
    String extractString = bundle.getString("imc.playlist.extract");
    String bpmNotReliableString = bundle.getString("imc.playlist.bpm_not_reliable");

    if (playlistItem.isSilentBreak()) {
      if (0 < playlistItem.getExtractDurationInMilliseconds()) {
        builder.append("    ");
        builder.append(formatTime.getStrictFormattedTime(totalDurationSoFar));
        builder.append(ITEM_DELIMITER);

        builder.append("[");
        builder.append(durationString);
        builder.append(" ");

        double extractDurationInSeconds = playlistItem.getExtractDurationInSeconds();
        if (playlist.getBlendMode() == CROSS) {
          extractDurationInSeconds -= playlist.getBlendTime();
        }
        builder.append(formatTime.getStrictFormattedTime(extractDurationInSeconds));

        builder.append("]");

        builder.append(EOL_DELIMITER);
      }
    }
    else {
      IAudioFile audioFile = playlistItem.getAudioFile();

      if (playlistItem instanceof BreakPlaylistItem) {
        builder.append("    ");
      }
      else {
        builder.append(String.format("%02d. ", listPosition + 1));
      }

      builder.append(formatTime.getStrictFormattedTime(totalDurationSoFar));
      builder.append(ITEM_DELIMITER);

      String bpmNotReliableStringInstance = bpmNotReliableString;

      if (audioFile.isBpmReliable()) {
        bpmNotReliableStringInstance = "";
      }

      builder.append("[");
      builder.append(durationString);
      builder.append(" ");

      double extractDurationInSeconds = playlistItem.getExtractDurationInSeconds();
      if (playlist.getBlendMode() == CROSS) {
        extractDurationInSeconds -= playlist.getBlendTime();
      }

      builder.append(formatTime.getStrictFormattedTime(extractDurationInSeconds));
      builder.append("]");
      builder.append(ITEM_DELIMITER);

      builder.append(audioFile.getDisplayName());
      builder.append(" ");

      builder.append("(");
      builder.append(audioFile.getBpm());
      builder.append(" bpm");
      builder.append(bpmNotReliableStringInstance);
      builder.append(")");
      builder.append(ITEM_DELIMITER);

      builder.append(extractString);
      builder.append(" ");
      builder.append(formatTime.getStrictFormattedTime(playlistItem.getExtractStartInSeconds()));
      builder.append(RANGE_DELIMITER);
      builder.append(formatTime.getStrictFormattedTime(playlistItem.getExtractEndInSeconds()));

      builder.append(EOL_DELIMITER);
    }

  }

  private void appendSoundEffectsList(StringBuilder builder, Playlist playlist) {
    if (playlist.hasSoundEffects()) {

      String soundEffectListTitle = bundle.getString("imc.playlist.soundEffectList_header");
      String durationString = bundle.getString("imc.playlist.duration");

      builder.append(EOL_DELIMITER);
      builder.append(EOL_DELIMITER);

      builder.append(soundEffectListTitle);
      builder.append(EOL_DELIMITER);

      builder.append(HORIZONTAL_ROW);
      builder.append(EOL_DELIMITER);

      for (SoundEffectOccurrence s : playlist.getSoundEffects()) {

        builder.append(formatTime.getStrictFormattedTime(s.getTimeMillis() / 1000));

        builder.append(", ");

        builder.append("[");
        builder.append(durationString);
        builder.append(" ");
        double extractDurationInSeconds = s.getSoundEffect().getDurationMillis() / 1000;
        builder.append(formatTime.getStrictFormattedTime(extractDurationInSeconds));
        builder.append("]");

        builder.append(", ");

        builder.append(s.getSoundEffect().getId());

        builder.append(EOL_DELIMITER);
      }

    }
  }

  private void appendBadTunesList(StringBuilder builder, List<IAudioFile> badTunesList) {
    if (badTunesList != null && !badTunesList.isEmpty()) {

      String errorListString = bundle.getString("imc.playlist.errorList_header");
      String reasonString = bundle.getString("imc.playlist.reason");
      String reasonTooShortString = bundle.getString("imc.playlist.reason_too_short");

      builder.append(EOL_DELIMITER);
      builder.append(EOL_DELIMITER);

      builder.append(errorListString);
      builder.append(EOL_DELIMITER);

      builder.append(HORIZONTAL_ROW);
      builder.append(EOL_DELIMITER);

      for (IAudioFile badTrack : badTunesList) {

        builder.append(badTrack.getDisplayName());
        builder.append(" -- ");

        if (badTrack.isOK()) {
          builder.append(reasonTooShortString);
        }
        else {
          builder.append(reasonString);
          builder.append(" ");
          builder.append(badTrack.getErrorMessage());
        }

        builder.append(EOL_DELIMITER);
      }
    }

  }

}
