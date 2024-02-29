package com.magic.modified;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.cipher.Cipher;
import com.github.kiulian.downloader.cipher.CipherFactory;
import com.github.kiulian.downloader.downloader.Downloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.request.RequestChannelUploads;
import com.github.kiulian.downloader.downloader.request.RequestPlaylistInfo;
import com.github.kiulian.downloader.downloader.request.RequestSearchContinuation;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.downloader.request.RequestSearchable;
import com.github.kiulian.downloader.downloader.request.RequestSubtitlesInfo;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.request.RequestWebpage;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.downloader.response.ResponseImpl;
import com.github.kiulian.downloader.extractor.Extractor;
import com.github.kiulian.downloader.model.playlist.PlaylistDetails;
import com.github.kiulian.downloader.model.playlist.PlaylistInfo;
import com.github.kiulian.downloader.model.playlist.PlaylistVideoDetails;
import com.github.kiulian.downloader.model.search.ContinuatedSearchResult;
import com.github.kiulian.downloader.model.search.SearchContinuation;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.SearchResultChannelDetails;
import com.github.kiulian.downloader.model.search.SearchResultElement;
import com.github.kiulian.downloader.model.search.SearchResultItem;
import com.github.kiulian.downloader.model.search.SearchResultPlaylistDetails;
import com.github.kiulian.downloader.model.search.SearchResultShelf;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.github.kiulian.downloader.model.search.query.QueryAutoCorrection;
import com.github.kiulian.downloader.model.search.query.QueryElement;
import com.github.kiulian.downloader.model.search.query.QueryElementType;
import com.github.kiulian.downloader.model.search.query.QueryRefinementList;
import com.github.kiulian.downloader.model.search.query.QuerySuggestion;
import com.github.kiulian.downloader.model.subtitles.SubtitlesInfo;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.Itag;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import com.github.kiulian.downloader.parser.Parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ParserImpl_Modified implements Parser {
    private static final String ANDROID_APIKEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
    private final Config config;
    private final Downloader downloader;
    private final Extractor extractor;
    private final CipherFactory cipherFactory;

    public ParserImpl_Modified(Config config, Downloader downloader, Extractor extractor, CipherFactory cipherFactory) {
        this.config = config;
        this.downloader = downloader;
        this.extractor = extractor;
        this.cipherFactory = cipherFactory;
    }

    public Response<VideoInfo> parseVideo(RequestVideoInfo request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<VideoInfo> result = executorService.submit(() -> {
                return this.parseVideo(request.getVideoId(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                VideoInfo result = this.parseVideo(request.getVideoId(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    private VideoInfo parseVideo(String videoId, YoutubeCallback<VideoInfo> callback) throws YoutubeException {
        VideoInfo videoInfo = this.parseVideoAndroid(videoId, callback);
        if (videoInfo == null) {
            videoInfo = this.parseVideoWeb(videoId, callback);
        }

        if (callback != null) {
            callback.onFinished(videoInfo);
        }

        return videoInfo;
    }

    private VideoInfo parseVideoAndroid(String videoId, YoutubeCallback<VideoInfo> callback) throws YoutubeException {
        String url = "https://youtubei.googleapis.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
        String body = "{  \"videoId\": \"" + videoId + "\",  \"context\": {    \"client\": {      \"hl\": \"en\",      \"gl\": \"US\",      \"clientName\": \"ANDROID_TESTSUITE\",      \"clientVersion\": \"1.9\",      \"androidSdkVersion\": 31    }  }}";
        RequestWebpage request = (RequestWebpage)(new RequestWebpage(url, "POST", body)).header("Content-Type", "application/json");
        Response<String> response = this.downloader.downloadWebpage(request);
        if (!response.ok()) {
            return null;
        } else {
            JSONObject playerResponse;
            try {
                playerResponse = JSONObject.parseObject((String)response.data());
            } catch (Exception var13) {
                return null;
            }

            VideoDetails videoDetails = this.parseVideoDetails(videoId, playerResponse);
            if (videoDetails.isDownloadable()) {
                JSONObject context = playerResponse.getJSONObject("responseContext");
                String clientVersion = this.extractor.extractClientVersionFromContext(context);

                List formats;
                try {
                    formats = this.parseFormats(playerResponse, (String)null, clientVersion);
                } catch (YoutubeException var14) {
                    if (callback != null) {
                        callback.onError(var14);
                    }

                    throw var14;
                }

                List<SubtitlesInfo> subtitlesInfo = this.parseCaptions(playerResponse);
                return new VideoInfo(videoDetails, formats, subtitlesInfo);
            } else {
                return new VideoInfo(videoDetails, Collections.emptyList(), Collections.emptyList());
            }
        }
    }

    private VideoInfo parseVideoWeb(String videoId, YoutubeCallback<VideoInfo> callback) throws YoutubeException {
        String htmlUrl = "https://www.youtube.com/watch?v=" + videoId;
        Response<String> response = this.downloader.downloadWebpage(new RequestWebpage(htmlUrl));
        if (!response.ok()) {
            YoutubeException e = new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", htmlUrl, response.error().getMessage()));
            if (callback != null) {
                callback.onError(e);
            }

            throw e;
        } else {
            String html = (String)response.data();

            JSONObject playerConfig;
            try {
                playerConfig = this.extractor.extractPlayerConfigFromHtml(html);
            } catch (YoutubeException var17) {
                if (callback != null) {
                    callback.onError(var17);
                }

                throw var17;
            }

            JSONObject args = playerConfig.getJSONObject("args");
            JSONObject playerResponse = args.getJSONObject("player_response");
            if (!playerResponse.containsKey("streamingData") && !playerResponse.containsKey("videoDetails")) {
                YoutubeException e = new YoutubeException.BadPageException("streamingData and videoDetails not found");
                if (callback != null) {
                    callback.onError(e);
                }

                throw e;
            } else {
                VideoDetails videoDetails = this.parseVideoDetails(videoId, playerResponse);
                if (videoDetails.isDownloadable()) {
                    String jsUrl;
                    try {
                        jsUrl = this.extractor.extractJsUrlFromConfig(playerConfig, videoId);
                    } catch (YoutubeException var15) {
                        if (callback != null) {
                            callback.onError(var15);
                        }

                        throw var15;
                    }

                    JSONObject context = playerConfig.getJSONObject("args").getJSONObject("player_response").getJSONObject("responseContext");
                    String clientVersion = this.extractor.extractClientVersionFromContext(context);

                    List formats;
                    try {
                        formats = this.parseFormats(playerResponse, jsUrl, clientVersion);
                    } catch (YoutubeException var16) {
                        if (callback != null) {
                            callback.onError(var16);
                        }

                        throw var16;
                    }

                    List<SubtitlesInfo> subtitlesInfo = this.parseCaptions(playerResponse);
                    return new VideoInfo(videoDetails, formats, subtitlesInfo);
                } else {
                    return new VideoInfo(videoDetails, Collections.emptyList(), Collections.emptyList());
                }
            }
        }
    }

    private VideoDetails parseVideoDetails(String videoId, JSONObject playerResponse) {
        if (!playerResponse.containsKey("videoDetails")) {
            return new VideoDetails(videoId);
        } else {
            JSONObject videoDetails = playerResponse.getJSONObject("videoDetails");
            String liveHLSUrl = null;
            if (videoDetails.getBooleanValue("isLive") && playerResponse.containsKey("streamingData")) {
                liveHLSUrl = playerResponse.getJSONObject("streamingData").getString("hlsManifestUrl");
            }

            return new VideoDetails(videoDetails, liveHLSUrl);
        }
    }

    private List<Format> parseFormats(JSONObject playerResponse, String jsUrl, String clientVersion) throws YoutubeException {
        if (!playerResponse.containsKey("streamingData")) {
            throw new YoutubeException.BadPageException("streamingData not found");
        } else {
            JSONObject streamingData = playerResponse.getJSONObject("streamingData");
            JSONArray jsonFormats = new JSONArray();
            if (streamingData.containsKey("formats")) {
                jsonFormats.addAll(streamingData.getJSONArray("formats"));
            }

            JSONArray jsonAdaptiveFormats = new JSONArray();
            if (streamingData.containsKey("adaptiveFormats")) {
                jsonAdaptiveFormats.addAll(streamingData.getJSONArray("adaptiveFormats"));
            }

            List<Format> formats = new ArrayList(jsonFormats.size() + jsonAdaptiveFormats.size());
            this.populateFormats(formats, jsonFormats, jsUrl, false, clientVersion);
            this.populateFormats(formats, jsonAdaptiveFormats, jsUrl, true, clientVersion);
            return formats;
        }
    }

    private void populateFormats(List<Format> formats, JSONArray jsonFormats, String jsUrl, boolean isAdaptive, String clientVersion) throws YoutubeException.CipherException {
        for(int i = 0; i < jsonFormats.size(); ++i) {
            JSONObject json = jsonFormats.getJSONObject(i);
            if (!"FORMAT_STREAM_TYPE_OTF".equals(json.getString("type"))) {
                int itagValue = json.getIntValue("itag");

                Itag itag;
                try {
                    itag = Itag.valueOf("i" + itagValue);
                } catch (IllegalArgumentException var14) {
                    System.err.println("Error parsing format: unknown itag " + itagValue);
                    continue;
                }

                try {
                    Format format = this.parseFormat(json, jsUrl, itag, isAdaptive, clientVersion);
                    formats.add(format);
                } catch (YoutubeException.CipherException var11) {
                    throw var11;
                } catch (YoutubeException var12) {
                    System.err.println("Error " + var12.getMessage() + " parsing format: " + json);
                } catch (Exception var13) {
                    var13.printStackTrace();
                }
            }
        }

    }

    private Format parseFormat(JSONObject json, String jsUrl, Itag itag, boolean isAdaptive, String clientVersion) throws YoutubeException {
        if (json.containsKey("signatureCipher")) {
            JSONObject jsonCipher = new JSONObject();
            String[] cipherData = json.getString("signatureCipher").replace("\\u0026", "&").split("&");
            String[] var8 = cipherData;
            int var9 = cipherData.length;

            String signature;
            for(int var10 = 0; var10 < var9; ++var10) {
                signature = var8[var10];
                String[] keyValue = signature.split("=");
                jsonCipher.put(keyValue[0], keyValue[1]);
            }

            if (!jsonCipher.containsKey("url")) {
                throw new YoutubeException.BadPageException("Could not found url in cipher data");
            }

            String urlWithSig = jsonCipher.getString("url");

            try {
                urlWithSig = URLDecoder.decode(urlWithSig, "UTF-8");
            } catch (UnsupportedEncodingException var14) {
                var14.printStackTrace();
            }

            if (!urlWithSig.contains("signature") && (jsonCipher.containsKey("s") || !urlWithSig.contains("&sig=") && !urlWithSig.contains("&lsig="))) {
                if (jsUrl == null) {
                    throw new YoutubeException.BadPageException("deciphering is required but no js url");
                }

                String s = jsonCipher.getString("s");

                try {
                    s = URLDecoder.decode(s, "UTF-8");
                } catch (UnsupportedEncodingException var13) {
                    var13.printStackTrace();
                }

                Cipher cipher = this.cipherFactory.createCipher(jsUrl);
                signature = cipher.getSignature(s);
                String decipheredUrl = urlWithSig + "&sig=" + signature;
                json.put("url", decipheredUrl);
            }
        }

        boolean hasVideo = itag.isVideo() || json.containsKey("size") || json.containsKey("width");
        boolean hasAudio = itag.isAudio() || json.containsKey("audioQuality");
        if (hasVideo && hasAudio) {
            return new VideoWithAudioFormat(json, isAdaptive, clientVersion);
        } else {
            return (Format)(hasVideo ? new VideoFormat(json, isAdaptive, clientVersion) : new AudioFormat(json, isAdaptive, clientVersion));
        }
    }

    private List<SubtitlesInfo> parseCaptions(JSONObject playerResponse) {
        if (!playerResponse.containsKey("captions")) {
            return Collections.emptyList();
        } else {
            JSONObject captions = playerResponse.getJSONObject("captions");
            JSONObject playerCaptionsTracklistRenderer = captions.getJSONObject("playerCaptionsTracklistRenderer");
            if (playerCaptionsTracklistRenderer != null && !playerCaptionsTracklistRenderer.isEmpty()) {
                JSONArray captionsArray = playerCaptionsTracklistRenderer.getJSONArray("captionTracks");
                if (captionsArray != null && !captionsArray.isEmpty()) {
                    List<SubtitlesInfo> subtitlesInfo = new ArrayList();

                    for(int i = 0; i < captionsArray.size(); ++i) {
                        JSONObject subtitleInfo = captionsArray.getJSONObject(i);
                        String language = subtitleInfo.getString("languageCode");
                        String url = subtitleInfo.getString("baseUrl");
                        String vssId = subtitleInfo.getString("vssId");
                        if (language != null && url != null && vssId != null) {
                            boolean isAutoGenerated = vssId.startsWith("a.");
                            subtitlesInfo.add(new SubtitlesInfo(url, language, isAutoGenerated, true));
                        }
                    }

                    return subtitlesInfo;
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    public Response<PlaylistInfo> parsePlaylist(RequestPlaylistInfo request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<PlaylistInfo> result = executorService.submit(() -> {
                return this.parsePlaylist(request.getPlaylistId(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                PlaylistInfo result = this.parsePlaylist(request.getPlaylistId(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    private PlaylistInfo parsePlaylist(String playlistId, YoutubeCallback<PlaylistInfo> callback) throws YoutubeException {
        String htmlUrl = "https://www.youtube.com/playlist?list=" + playlistId;
        Response<String> response = this.downloader.downloadWebpage(new RequestWebpage(htmlUrl));
        if (!response.ok()) {
            YoutubeException e = new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", htmlUrl, response.error().getMessage()));
            if (callback != null) {
                callback.onError(e);
            }

            throw e;
        } else {
            String html = (String)response.data();

            JSONObject initialData;
            try {
                initialData = this.extractor.extractInitialDataFromHtml(html);
            } catch (YoutubeException var11) {
                if (callback != null) {
                    callback.onError(var11);
                }

                throw var11;
            }

            if (!initialData.containsKey("metadata")) {
                throw new YoutubeException.BadPageException("Invalid initial data json");
            } else {
                PlaylistDetails playlistDetails = this.parsePlaylistDetails(playlistId, initialData);

                List videos;
                try {
                    videos = this.parsePlaylistVideos(initialData, playlistDetails.videoCount());
                } catch (YoutubeException var10) {
                    if (callback != null) {
                        callback.onError(var10);
                    }

                    throw var10;
                }

                return new PlaylistInfo(playlistDetails, videos);
            }
        }
    }

    private PlaylistDetails parsePlaylistDetails(String playlistId, JSONObject initialData) {
        String title = initialData.getJSONObject("metadata").getJSONObject("playlistMetadataRenderer").getString("title");
        JSONArray sideBarItems = initialData.getJSONObject("sidebar").getJSONObject("playlistSidebarRenderer").getJSONArray("items");
        String author = null;

        try {
            author = sideBarItems.getJSONObject(1).getJSONObject("playlistSidebarSecondaryInfoRenderer").getJSONObject("videoOwner").getJSONObject("videoOwnerRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text");
        } catch (Exception var10) {
        }

        JSONArray stats = sideBarItems.getJSONObject(0).getJSONObject("playlistSidebarPrimaryInfoRenderer").getJSONArray("stats");
        int videoCount = this.extractor.extractIntegerFromText(stats.getJSONObject(0).getJSONArray("runs").getJSONObject(0).getString("text"));
        long viewCount = this.extractor.extractLongFromText(stats.getJSONObject(1).getString("simpleText"));
        return new PlaylistDetails(playlistId, title, author, videoCount, viewCount);
    }

    private List<PlaylistVideoDetails> parsePlaylistVideos(JSONObject initialData, int videoCount) throws YoutubeException {
        JSONObject content;
        try {
            content = initialData.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer").getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer").getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("playlistVideoListRenderer");
        } catch (NullPointerException var7) {
            throw new YoutubeException.BadPageException("Playlist initial data not found");
        }

        Object videos;
        if (videoCount > 0) {
            videos = new ArrayList(videoCount);
        } else {
            videos = new LinkedList();
        }

        JSONObject context = initialData.getJSONObject("responseContext");
        String clientVersion = this.extractor.extractClientVersionFromContext(context);
        this.populatePlaylist(content, (List)videos, clientVersion);
        return (List)videos;
    }

    private void populatePlaylist(JSONObject content, List<PlaylistVideoDetails> videos, String clientVersion) throws YoutubeException {
        JSONArray contents;
        if (content.containsKey("contents")) {
            contents = content.getJSONArray("contents");
        } else {
            if (!content.containsKey("continuationItems")) {
                if (content.containsKey("continuations")) {
                    JSONObject nextContinuationData = content.getJSONArray("continuations").getJSONObject(0).getJSONObject("nextContinuationData");
                    String continuation = nextContinuationData.getString("continuation");
                    String ctp = nextContinuationData.getString("clickTrackingParams");
                    this.loadPlaylistContinuation(continuation, ctp, videos, clientVersion);
                    return;
                }

                return;
            }

            contents = content.getJSONArray("continuationItems");
        }

        for(int i = 0; i < contents.size(); ++i) {
            JSONObject contentsItem = contents.getJSONObject(i);
            if (contentsItem.containsKey("playlistVideoRenderer")) {
                videos.add(new PlaylistVideoDetails(contentsItem.getJSONObject("playlistVideoRenderer")));
            } else if (contentsItem.containsKey("continuationItemRenderer")) {
                JSONObject continuationEndpoint = contentsItem.getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint");
                String continuation = continuationEndpoint.getJSONObject("continuationCommand").getString("token");
                String ctp = continuationEndpoint.getString("clickTrackingParams");
                this.loadPlaylistContinuation(continuation, ctp, videos, clientVersion);
            }
        }

    }

    private void loadPlaylistContinuation(String continuation, String ctp, List<PlaylistVideoDetails> videos, String clientVersion) throws YoutubeException {
        String url = "https://www.youtube.com/youtubei/v1/browse?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
        JSONObject body = (new JSONObject()).fluentPut("context", (new JSONObject()).fluentPut("client", (new JSONObject()).fluentPut("clientName", "WEB").fluentPut("clientVersion", "2.20201021.03.00"))).fluentPut("continuation", continuation).fluentPut("clickTracking", (new JSONObject()).fluentPut("clickTrackingParams", ctp));
        RequestWebpage request = (RequestWebpage)((RequestWebpage)((RequestWebpage)(new RequestWebpage(url, "POST", body.toJSONString())).header("X-YouTube-Client-Name", "1")).header("X-YouTube-Client-Version", clientVersion)).header("Content-Type", "application/json");
        Response<String> response = this.downloader.downloadWebpage(request);
        if (!response.ok()) {
            throw new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", url, response.error().getMessage()));
        } else {
            String html = (String)response.data();

            try {
                JSONObject jsonResponse = JSON.parseObject(html);
                JSONObject content;
                if (jsonResponse.containsKey("continuationContents")) {
                    content = jsonResponse.getJSONObject("continuationContents").getJSONObject("playlistVideoListContinuation");
                } else {
                    content = jsonResponse.getJSONArray("onResponseReceivedActions").getJSONObject(0).getJSONObject("appendContinuationItemsAction");
                }

                this.populatePlaylist(content, videos, clientVersion);
            } catch (YoutubeException var12) {
                throw var12;
            } catch (Exception var13) {
                throw new YoutubeException.BadPageException("Could not parse playlist continuation json");
            }
        }
    }

    public Response<PlaylistInfo> parseChannelsUploads(RequestChannelUploads request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<PlaylistInfo> result = executorService.submit(() -> {
                return this.parseChannelsUploads(request.getChannelId(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                PlaylistInfo result = this.parseChannelsUploads(request.getChannelId(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    private PlaylistInfo parseChannelsUploads(String channelId, YoutubeCallback<PlaylistInfo> callback) throws YoutubeException {
        String playlistId = null;
        if (channelId.length() == 24 && channelId.startsWith("UC")) {
            playlistId = "UU" + channelId.substring(2);
        } else {
            String channelLink = "https://www.youtube.com/c/" + channelId + "/videos?view=57";
            Response<String> response = this.downloader.downloadWebpage(new RequestWebpage(channelLink));
            if (!response.ok()) {
                YoutubeException e = new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", channelLink, response.error().getMessage()));
                if (callback != null) {
                    callback.onError(e);
                }

                throw e;
            }

            String html = (String)response.data();
            Scanner scan = new Scanner(html);
            scan.useDelimiter("list=");

            while(scan.hasNext()) {
                String pId = scan.next();
                if (pId.startsWith("UU")) {
                    playlistId = pId.substring(0, 24);
                    break;
                }
            }
        }

        if (playlistId == null) {
            YoutubeException e = new YoutubeException.BadPageException("Upload Playlist not found");
            if (callback != null) {
                callback.onError(e);
            }

            throw e;
        } else {
            return this.parsePlaylist(playlistId, callback);
        }
    }

    public Response<List<SubtitlesInfo>> parseSubtitlesInfo(RequestSubtitlesInfo request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<List<SubtitlesInfo>> result = executorService.submit(() -> {
                return this.parseSubtitlesInfo(request.getVideoId(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                List<SubtitlesInfo> result = this.parseSubtitlesInfo(request.getVideoId(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    private List<SubtitlesInfo> parseSubtitlesInfo(String videoId, YoutubeCallback<List<SubtitlesInfo>> callback) throws YoutubeException {
        String xmlUrl = "https://video.google.com/timedtext?hl=en&type=list&v=" + videoId;
        Response<String> response = this.downloader.downloadWebpage(new RequestWebpage(xmlUrl));
        if (!response.ok()) {
            YoutubeException e = new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", xmlUrl, response.error().getMessage()));
            if (callback != null) {
                callback.onError(e);
            }

            throw e;
        } else {
            String xml = (String)response.data();

            List languages;
            try {
                languages = this.extractor.extractSubtitlesLanguagesFromXml(xml);
            } catch (YoutubeException var11) {
                if (callback != null) {
                    callback.onError(var11);
                }

                throw var11;
            }

            List<SubtitlesInfo> subtitlesInfo = new ArrayList();
            Iterator var8 = languages.iterator();

            while(var8.hasNext()) {
                String language = (String)var8.next();
                String url = String.format("https://www.youtube.com/api/timedtext?lang=%s&v=%s", language, videoId);
                subtitlesInfo.add(new SubtitlesInfo(url, language, false));
            }

            return subtitlesInfo;
        }
    }

    public Response<SearchResult> parseSearchResult(RequestSearchResult request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<SearchResult> result = executorService.submit(() -> {
                return this.parseSearchResult(request.query(), request.encodeParameters(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                SearchResult result = this.parseSearchResult(request.query(), request.encodeParameters(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    public Response<SearchResult> parseSearchContinuation(RequestSearchContinuation request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<SearchResult> result = executorService.submit(() -> {
                return this.parseSearchContinuation(request.continuation(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                SearchResult result = this.parseSearchContinuation(request.continuation(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    public Response<SearchResult> parseSearcheable(RequestSearchable request) {
        if (request.isAsync()) {
            ExecutorService executorService = this.config.getExecutorService();
            Future<SearchResult> result = executorService.submit(() -> {
                return this.parseSearchable(request.searchPath(), request.getCallback());
            });
            return ResponseImpl.fromFuture(result);
        } else {
            try {
                SearchResult result = this.parseSearchable(request.searchPath(), request.getCallback());
                return ResponseImpl.from(result);
            } catch (YoutubeException var4) {
                return ResponseImpl.error(var4);
            }
        }
    }

    private SearchResult parseSearchResult(String query, String parameters, YoutubeCallback<SearchResult> callback) throws YoutubeException {
        String searchQuery;
        try {
            searchQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException var7) {
            searchQuery = query;
            var7.printStackTrace();
        }

        String url = "https://www.youtube.com/results?search_query=" + searchQuery;
        if (parameters != null) {
            url = url + "&sp=" + parameters;
        }

        try {
            return this.parseHtmlSearchResult(url);
        } catch (YoutubeException var8) {
            if (callback != null) {
                callback.onError(var8);
            }

            throw var8;
        }
    }

    private SearchResult parseSearchable(String searchPath, YoutubeCallback<SearchResult> callback) throws YoutubeException {
        String url = "https://www.youtube.com" + searchPath;

        try {
            return this.parseHtmlSearchResult(url);
        } catch (YoutubeException var5) {
            if (callback != null) {
                callback.onError(var5);
            }

            throw var5;
        }
    }

    private SearchResult parseHtmlSearchResult(String url) throws YoutubeException {
        Response<String> response = this.downloader.downloadWebpage(new RequestWebpage(url));
        if (!response.ok()) {
            throw new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", url, response.error().getMessage()));
        } else {
            String html = (String)response.data();
            JSONObject initialData = this.extractor.extractInitialDataFromHtml(html);

            JSONArray rootContents;
            try {
                rootContents = initialData.getJSONObject("contents").getJSONObject("twoColumnSearchResultsRenderer").getJSONObject("primaryContents").getJSONObject("sectionListRenderer").getJSONArray("contents");
            } catch (NullPointerException var10) {
                throw new YoutubeException.BadPageException("Search result root contents not found");
            }

            long estimatedCount = this.extractor.extractLongFromText(initialData.getString("estimatedResults"));
            String clientVersion = this.extractor.extractClientVersionFromContext(initialData.getJSONObject("responseContext"));
            SearchContinuation continuation = this.getSearchContinuation(rootContents, clientVersion);
            return this.parseSearchResult(estimatedCount, rootContents, continuation);
        }
    }

    private SearchResult parseSearchContinuation(SearchContinuation continuation, YoutubeCallback<SearchResult> callback) throws YoutubeException {
        String url = "https://www.youtube.com/youtubei/v1/search?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false";
        JSONObject body = (new JSONObject()).fluentPut("context", (new JSONObject()).fluentPut("client", (new JSONObject()).fluentPut("clientName", "WEB").fluentPut("clientVersion", "2.20201021.03.00"))).fluentPut("continuation", continuation.token()).fluentPut("clickTracking", (new JSONObject()).fluentPut("clickTrackingParams", continuation.clickTrackingParameters()));
        RequestWebpage request = (RequestWebpage)((RequestWebpage)((RequestWebpage)(new RequestWebpage(url, "POST", body.toJSONString())).header("X-YouTube-Client-Name", "1")).header("X-YouTube-Client-Version", continuation.clientVersion())).header("Content-Type", "application/json");
        Response<String> response = this.downloader.downloadWebpage(request);
        if (!response.ok()) {
            YoutubeException e = new YoutubeException.DownloadException(String.format("Could not load url: %s, exception: %s", url, response.error().getMessage()));
            if (callback != null) {
                callback.onError(e);
            }

            throw e;
        } else {
            String html = (String)response.data();

            JSONObject jsonResponse;
            JSONArray rootContents;
            try {
                jsonResponse = JSON.parseObject(html);
                if (!jsonResponse.containsKey("onResponseReceivedCommands")) {
                    throw new YoutubeException.BadPageException("Could not find continuation data");
                }

                rootContents = jsonResponse.getJSONArray("onResponseReceivedCommands").getJSONObject(0).getJSONObject("appendContinuationItemsAction").getJSONArray("continuationItems");
            } catch (YoutubeException var13) {
                throw var13;
            } catch (Exception var14) {
                throw new YoutubeException.BadPageException("Could not parse search continuation json");
            }

            long estimatedResults = this.extractor.extractLongFromText(jsonResponse.getString("estimatedResults"));
            SearchContinuation nextContinuation = this.getSearchContinuation(rootContents, continuation.clientVersion());
            return this.parseSearchResult(estimatedResults, rootContents, nextContinuation);
        }
    }

    private SearchContinuation getSearchContinuation(JSONArray rootContents, String clientVersion) {
        if (rootContents.size() > 1 && rootContents.getJSONObject(1).containsKey("continuationItemRenderer")) {
            JSONObject endPoint = rootContents.getJSONObject(1).getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint");
            String token = endPoint.getJSONObject("continuationCommand").getString("token");
            String ctp = endPoint.getString("clickTrackingParams");
            return new SearchContinuation(token, clientVersion, ctp);
        } else {
            return null;
        }
    }

    private SearchResult parseSearchResult(long estimatedResults, JSONArray rootContents, SearchContinuation continuation) throws YoutubeException.BadPageException {
        JSONArray contents;
        try {
            contents = rootContents.getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents");
        } catch (NullPointerException var11) {
            throw new YoutubeException.BadPageException("Search result contents not found");
        }

        List<SearchResultItem> items = new ArrayList(contents.size());
        Map<QueryElementType, QueryElement> queryElements = new HashMap();

        for(int i = 0; i < contents.size(); ++i) {
            SearchResultElement element = parseSearchResultElement(contents.getJSONObject(i));
            if (element != null) {
                if (element instanceof SearchResultItem) {
                    items.add((SearchResultItem)element);
                } else {
                    QueryElement queryElement = (QueryElement)element;
                    queryElements.put(queryElement.type(), queryElement);
                }
            }
        }

        if (continuation == null) {
            return new SearchResult(estimatedResults, items, queryElements);
        } else {
            return new ContinuatedSearchResult(estimatedResults, items, queryElements, continuation);
        }
    }

    private static SearchResultElement parseSearchResultElement(JSONObject jsonItem) {
        String rendererKey = (String)jsonItem.keySet().iterator().next();
        JSONObject jsonRenderer = jsonItem.getJSONObject(rendererKey);
        switch (rendererKey) {
            case "videoRenderer":
                return new SearchResultVideoDetails(jsonRenderer, false);
            case "movieRenderer":
                return new SearchResultVideoDetails(jsonRenderer, true);
            case "playlistRenderer":
                return new SearchResultPlaylistDetails(jsonRenderer);
            case "channelRenderer":
                return new SearchResultChannelDetails(jsonRenderer);
            case "shelfRenderer":
                return new SearchResultShelf(jsonRenderer);
            case "showingResultsForRenderer":
                return new QueryAutoCorrection(jsonRenderer);
            case "didYouMeanRenderer":
                return new QuerySuggestion(jsonRenderer);
            case "horizontalCardListRenderer":
                return new QueryRefinementList(jsonRenderer);
            default:
                return null;
        }
    }
}
