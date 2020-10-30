package com.sedmelluq.lavaplayer.core.container.playlists;

import com.sedmelluq.lavaplayer.core.http.HttpInterface;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.sedmelluq.lavaplayer.core.http.HttpClientTools.fetchResponseLines;

public class HlsStreamSegmentUrlProvider extends M3uStreamSegmentUrlProvider {
  private static final Logger log = LoggerFactory.getLogger(HlsStreamSegmentUrlProvider.class);

  private final String streamListUrl;
  private volatile String segmentPlaylistUrl;

  public HlsStreamSegmentUrlProvider(String streamListUrl, String segmentPlaylistUrl) {
    this.streamListUrl = streamListUrl;
    this.segmentPlaylistUrl = segmentPlaylistUrl;
  }

  @Override
  protected String getQualityFromM3uDirective(ExtendedM3uParser.Line directiveLine) {
    return "default";
  }

  @Override
  protected String fetchSegmentPlaylistUrl(HttpInterface httpInterface) throws IOException {
    if (segmentPlaylistUrl != null) {
      return segmentPlaylistUrl;
    }

    HttpUriRequest request = new HttpGet(streamListUrl);
    List<ChannelStreamInfo> streams = loadChannelStreamsList(fetchResponseLines(httpInterface, request,
        "HLS stream list"));

    if (streams.isEmpty()) {
      throw new IllegalStateException("No streams listed in HLS stream list.");
    }

    ChannelStreamInfo stream = streams.get(0);

    log.debug("Chose stream with url {}", stream.quality, stream.url);
    segmentPlaylistUrl = stream.url;
    return segmentPlaylistUrl;
  }

  @Override
  protected HttpUriRequest createSegmentGetRequest(String url) {
    return new HttpGet(url);
  }

  public static String findHlsEntryUrl(String[] lines) {
    List<ChannelStreamInfo> streams = new HlsStreamSegmentUrlProvider(null, null)
        .loadChannelStreamsList(lines);

    return streams.isEmpty() ? null : streams.get(0).url;
  }
}