package ch.retorte.intervalmusiccompositor.playlist;

import static ch.retorte.intervalmusiccompositor.model.list.BlendMode.CROSS;
import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;
import static java.util.Comparator.comparingLong;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import ch.retorte.intervalmusiccompositor.core.bundle.CoreBundleProvider;
import ch.retorte.intervalmusiccompositor.model.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.bundle.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.model.soundeffect.SoundEffectOccurrence;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;

/**
 * @author nw
 */
public class PlaylistReport {

  private MessageFormatBundle bundle = new CoreBundleProvider().getBundle();

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

      PlaylistItemFragment musicFragment = playlistItem.getMusicFragment();

      int currentPositionsSeconds = totalPositionSeconds;
      if (playlist.isCrossFadingMode()) {
        currentPositionsSeconds += playlist.getHalfBlendTimeS();
      }
      appendMusicLine(result, playlist, i, currentPositionsSeconds, musicFragment);
      totalPositionSeconds += musicFragment.getExtractDurationInSeconds();

      if (playlist.isCrossFadingMode()) {
        totalPositionSeconds -= playlist.getBlendTimeS();
      }

      if (playlistItem.hasBreakFragment()) {
        PlaylistItemFragment breakFragment = playlistItem.getBreakFragment();

        currentPositionsSeconds = totalPositionSeconds;
        if (playlist.isCrossFadingMode()) {
          currentPositionsSeconds += playlist.getHalfBlendTimeS();
        }

        appendMusicLine(result, playlist, i, currentPositionsSeconds, breakFragment);
        totalPositionSeconds += breakFragment.getExtractDurationInSeconds();

        if (playlist.isCrossFadingMode()) {
          totalPositionSeconds -= playlist.getBlendTimeS();
        }
      }

      i++;
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

  private void appendMusicLine(StringBuilder builder, Playlist playlist, int listPosition, int totalDurationSoFar, PlaylistItemFragment playlistItemFragment) {

    String durationString = bundle.getString("imc.playlist.duration");
    String extractString = bundle.getString("imc.playlist.extract");
    String bpmNotReliableString = bundle.getString("imc.playlist.bpm_not_reliable");

    if (playlistItemFragment.isSilentBreak()) {
      if (0 < playlistItemFragment.getExtractDurationInMilliseconds()) {
        builder.append("    ");
        builder.append(formatTime.getStrictFormattedTime(totalDurationSoFar));
        builder.append(ITEM_DELIMITER);

        builder.append("[");
        builder.append(durationString);
        builder.append(" ");

        double extractDurationInSeconds = playlistItemFragment.getExtractDurationInSeconds();
        if (playlist.getBlendMode() == CROSS) {
          extractDurationInSeconds -= playlist.getBlendTimeS();
        }
        builder.append(formatTime.getStrictFormattedTime(extractDurationInSeconds));

        builder.append("]");

        builder.append(EOL_DELIMITER);
      }
    }
    else {
      IAudioFile audioFile = playlistItemFragment.getAudioFile();

      if (playlistItemFragment instanceof BreakPlaylistItemFragment) {
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

      double extractDurationInSeconds = playlistItemFragment.getExtractDurationInSeconds();
      if (playlist.getBlendMode() == CROSS) {
        extractDurationInSeconds -= playlist.getBlendTimeS();
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
      builder.append(formatTime.getStrictFormattedTime(playlistItemFragment.getExtractStartInSeconds()));
      builder.append(RANGE_DELIMITER);
      builder.append(formatTime.getStrictFormattedTime(playlistItemFragment.getExtractEndInSeconds()));

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

      long totalTimeMs = 0;
      if (playlist.isCrossFadingMode()) {
        totalTimeMs += playlist.getHalfBlendTimeMs();
      }
      for (PlaylistItem p : playlist) {

        for (SoundEffectOccurrence s : sortByStartTime(p.getSoundEffects())) {

          builder.append(formatTime.getStrictFormattedTime((totalTimeMs + s.getTimeMillis()) / 1000.0));

          builder.append(", ");

          builder.append("[");
          builder.append(durationString);
          builder.append(" ");
          double extractDurationInSeconds = s.getSoundEffect().getDisplayDurationMillis() / 1000.0;
          builder.append(formatTime.getStrictFormattedTime(extractDurationInSeconds));
          builder.append("]");

          builder.append(", ");

          builder.append(s.getSoundEffect().getId());

          builder.append(EOL_DELIMITER);

        }

        totalTimeMs += p.getStrictItemLengthMs();
      }

    }
  }

  private List<SoundEffectOccurrence> sortByStartTime(List<SoundEffectOccurrence> soundEffectOccurrences) {
    List<SoundEffectOccurrence> result = newArrayList(soundEffectOccurrences);
    result.sort(comparingLong(SoundEffectOccurrence::getTimeMillis));
    return result;
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
