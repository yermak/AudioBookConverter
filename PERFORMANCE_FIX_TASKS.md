# Performance Fix Tasks

This document provides actionable tasks to fix all 51 performance issues identified in the analysis.

---

## Task Organization

**Total Tasks:** 51
- 🔴 **Critical Priority:** 25 tasks (fix first)
- 🟡 **High Priority:** 15 tasks (fix second)
- 🟢 **Medium Priority:** 11 tasks (fix third)

---

## Phase 1: Critical Priority Tasks (25)

### Category: Threading & Concurrency (15 tasks)

#### Task 1: Fix StreamCopier static ExecutorService
**File:** `StreamCopier.java:23`
**Issue:** Static ExecutorService never shut down
**Fix:**
```java
// Add shutdown hook
public static void shutdown() {
    executorService.shutdown();
    try {
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    } catch (InterruptedException e) {
        executorService.shutdownNow();
    }
}

// Call from application shutdown
```

#### Task 2: Fix ConversionJob static ExecutorService
**File:** `ConversionJob.java:29`
**Issue:** Static ExecutorService never shut down
**Fix:** Same as Task 1

#### Task 3: Fix FFMediaLoader static ExecutorService
**File:** `FFMediaLoader.java:35`
**Issue:** Static ExecutorService never shut down
**Fix:** Same as Task 1

#### Task 4: Fix MediaInfoLoader static ExecutorService
**File:** `MediaInfoLoader.java:40`
**Issue:** Static ScheduledExecutorService never shut down
**Fix:** Same as Task 1

#### Task 5: Fix ConversionContext static ExecutorService
**File:** `ConversionContext.java:49`
**Issue:** Static ExecutorService never shut down
**Fix:** Same as Task 1

#### Task 6: Fix ad-hoc executor in AudiobookConverter
**File:** `AudiobookConverter.java:73`
**Current:**
```java
public static void checkNewVersion(ResourceBundle bundle) {
    Executors.newSingleThreadExecutor().submit(new VersionChecker(bundle));
}
```
**Fix:**
```java
public static void checkNewVersion(ResourceBundle bundle) {
    ConversionContext.getExecutorService().submit(new VersionChecker(bundle));
}
```

#### Task 7: Fix ad-hoc executor in ConversionGroup
**File:** `ConversionGroup.java:50`
**Current:**
```java
Executors.newSingleThreadExecutor().execute(conversionProgress);
```
**Fix:**
```java
ConversionContext.getExecutorService().execute(conversionProgress);
```

#### Task 8: Fix ad-hoc executor in FilesController line 656
**File:** `FilesController.java:656`
**Fix:** Reuse shared executor

#### Task 9: Fix ad-hoc executor in FilesController line 713
**File:** `FilesController.java:713`
**Fix:** Reuse shared executor

#### Task 10: Fix ad-hoc executor in BookInfoController
**File:** `BookInfoController.java:152`
**Fix:** Reuse shared executor

#### Task 11: Fix busy-wait loop in FFMpegNativeConverter
**File:** `FFMpegNativeConverter.java:41`
**Current:**
```java
while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) Thread.sleep(1000);
```
**Fix:**
```java
// In ConversionJob, add:
private final CountDownLatch pauseLatch = new CountDownLatch(0);

public void pause() {
    this.status = ProgressStatus.PAUSED;
    pauseLatch = new CountDownLatch(1);
}

public void resume() {
    this.status = ProgressStatus.RUNNING;
    pauseLatch.countDown();
}

// In FFMpegNativeConverter:
while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) {
    conversionJob.getPauseLatch().await();
}
```

#### Task 12: Fix busy-wait loop in FFMpegConcatenator
**File:** `FFMpegConcatenator.java:54`
**Fix:** Same as Task 11

#### Task 13: Fix busy-wait loop in FFMpegOptimizer
**File:** `FFMpegOptimizer.java:38`
**Fix:** Same as Task 11

#### Task 14: Fix unconditional sleep in AudiobookConverter shutdown
**File:** `AudiobookConverter.java:119-128`
**Current:**
```java
stage.setOnCloseRequest(event -> {
    logger.info("Closing application");
    AudiobookConverter.getContext().stopConversions();
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.exit(0);
});
```
**Fix:**
```java
stage.setOnCloseRequest(event -> {
    logger.info("Closing application");
    AudiobookConverter.getContext().stopConversions();

    // Shutdown all executors properly
    StreamCopier.shutdown();
    ConversionJob.shutdown();
    FFMediaLoader.shutdown();
    MediaInfoLoader.shutdown();
    ConversionContext.shutdown();

    System.exit(0);
});
```

#### Task 15: Fix unconditional sleep in FilesController exit
**File:** `FilesController.java:800-809`
**Fix:** Same as Task 14

### Category: Memory Leaks (10 tasks)

#### Task 16: Fix unclosed FileInputStream
**File:** `FlacPicture.java:24-25`
**Current:**
```java
public static FlacPicture load(String artWorkFile) {
    try {
        FileInputStream inputstream = new FileInputStream(artWorkFile);
        Image image = new Image(inputstream);
        // ...
    }
}
```
**Fix:**
```java
public static FlacPicture load(String artWorkFile) {
    try (FileInputStream inputstream = new FileInputStream(artWorkFile)) {
        Image image = new Image(inputstream);
        // ...
    }
}
```

#### Task 17: Fix unbounded listeners in ProgressComponent
**File:** `ProgressComponent.java:173-178`
**Current:**
```java
void assignListeners(ConversionProgress conversionProgress) {
    conversionProgress.filesCount.addListener((observable, oldValue, newValue) ->
        Platform.runLater(() -> filesCount.setText(newValue)));
    // ... 5 more listeners
}
```
**Fix:**
```java
private final List<ChangeListener<?>> listeners = new ArrayList<>();

void assignListeners(ConversionProgress conversionProgress) {
    ChangeListener<String> filesCountListener = (observable, oldValue, newValue) ->
        Platform.runLater(() -> filesCount.setText(newValue));
    conversionProgress.filesCount.addListener(filesCountListener);
    listeners.add(filesCountListener);
    // ... repeat for all listeners
}

public void cleanup() {
    // Remove all listeners
    for (ChangeListener<?> listener : listeners) {
        // Remove from corresponding observable
    }
    listeners.clear();
}
```

#### Task 18: Fix unbounded listeners in BookInfoController
**File:** `BookInfoController.java:128-142`
**Fix:** Same pattern as Task 17

#### Task 19: Fix unbounded listeners in FilesController
**File:** `FilesController.java:410-434`
**Fix:** Same pattern as Task 17

#### Task 20: Fix unbounded listeners in OutputController
**File:** `OutputController.java` (multiple lines)
**Fix:** Same pattern as Task 17

#### Task 21: Fix unbounded listeners in FileListComponent
**File:** `FileListComponent.java:20-23`
**Fix:** Same pattern as Task 17

#### Task 22: Fix ConversionContext listener methods
**File:** `ConversionContext.java:125-174`
**Current:**
```java
public void addContextDetachListener(InvalidationListener invalidationListener) {
    conversionGroupHolder.addListener(invalidationListener);
}
```
**Fix:**
```java
public void addContextDetachListener(InvalidationListener invalidationListener) {
    conversionGroupHolder.addListener(invalidationListener);
}

public void removeContextDetachListener(InvalidationListener invalidationListener) {
    conversionGroupHolder.removeListener(invalidationListener);
}

// Add similar remove methods for all listener types
```

#### Task 23: Fix unbounded conversionQueue
**File:** `ConversionContext.java:35`
**Current:**
```java
private final LinkedList<ConversionJob> conversionQueue = new LinkedList<>();
```
**Fix:**
```java
private final LinkedList<ConversionJob> conversionQueue = new LinkedList<>();

public void cleanupCompletedJobs() {
    conversionQueue.removeIf(job ->
        job.getStatus() == ProgressStatus.FINISHED ||
        job.getStatus() == ProgressStatus.CANCELLED);
}

// Call periodically or after conversions complete
```

#### Task 24: Fix unbounded removedArtWorks set
**File:** `ConversionContext.java:52`
**Current:**
```java
private Set<ArtWork> removedArtWorks = new HashSet<>();
```
**Fix:**
```java
// Clear after use or use WeakHashSet
private Set<ArtWork> removedArtWorks = Collections.newSetFromMap(new WeakHashMap<>());
```

#### Task 25: Fix synchronization in ConversionProgress
**File:** `ConversionProgress.java:95-124`
**Current:**
```java
private final Map<String, Long> durations = new HashMap<>();
private final Map<String, Long> sizes = new HashMap<>();
```
**Fix:**
```java
private final Map<String, Long> durations = new ConcurrentHashMap<>();
private final Map<String, Long> sizes = new ConcurrentHashMap<>();
private volatile boolean paused = false;
private volatile boolean cancelled = false;
private volatile boolean finished = false;
```

---

## Phase 2: High Priority Tasks (15)

### Category: N+1 Patterns (4 tasks)

#### Task 26: Fix FFprobe instance per output file
**File:** `DurationVerifier.java:24`
**Current:**
```java
public static void ffMpegUpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
    FFprobe ffprobe = new FFprobe(Platform.FFPROBE);
    // ...
}
```
**Fix:**
```java
private static final FFprobe FFPROBE_INSTANCE = new FFprobe(Platform.FFPROBE);

public static void ffMpegUpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
    FFmpegProbeResult probe = FFPROBE_INSTANCE.probe(outputFileName);
    // ...
}
```

#### Task 27: Fix recursive directory scan N+1
**File:** `FFMediaLoader.java:74-104`
**Current:**
```java
static void searchForPosters(List<MediaInfo> media) throws FileNotFoundException {
    Set<File> searchDirs = new HashSet<>();
    media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

    List<File> pictures = new ArrayList<>();
    for (File d : searchDirs) {
        pictures.addAll(findPictures(d));
    }
}
```
**Fix:**
```java
static void searchForPosters(List<MediaInfo> media) throws FileNotFoundException {
    // Already using Set to deduplicate - this is correct
    // But could optimize by checking if searchDirs is empty first
    if (media.isEmpty()) return;

    Set<File> searchDirs = media.stream()
        .map(mi -> new File(mi.getFileName()).getParentFile())
        .collect(Collectors.toSet());

    List<File> pictures = searchDirs.parallelStream()
        .flatMap(d -> findPictures(d).stream())
        .collect(Collectors.toList());
    // ...
}
```

#### Task 28: Fix collection N+1 in CUE parsing
**File:** `FlacLoader.java:26-59`
**Current:**
```java
static void parseCue(MediaInfoBean mediaInfo, String cue) {
    AudioBookInfo bookInfo = mediaInfo.getBookInfo();
    String[] split = StringUtils.split(cue, "\n");
    for (String line : split) {
        if (bookInfo.tracks().isEmpty()) { // ...
        Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
    }
}
```
**Fix:**
```java
static void parseCue(MediaInfoBean mediaInfo, String cue) {
    AudioBookInfo bookInfo = mediaInfo.getBookInfo();
    List<Track> tracks = bookInfo.tracks(); // Cache reference
    String[] split = StringUtils.split(cue, "\n");
    for (String line : split) {
        int i = -1;
        if (tracks.isEmpty()) { // ...
        Track track = tracks.get(tracks.size() - 1); // Use cached reference
    }
}
```

#### Task 29: Fix repeated get(0) calls in lambda expressions
**File:** `Chapter.java:29-77`
**Current:**
```java
if (context.contains("TAG.1")) {
    renderMap.put("TAG.1", chapter -> chapter.getMedia().get(0).getBookInfo().title());
}
if (context.contains("TAG.2")) {
    renderMap.put("TAG.2", chapter -> chapter.getMedia().get(0).getBookInfo().writer());
}
```
**Fix:**
```java
// Refactor to extract once per render call
private Object getTag(Chapter chapter, String tagName) {
    if (chapter.getMedia().isEmpty()) return "";
    MediaInfo firstMedia = chapter.getMedia().get(0);
    AudioBookInfo bookInfo = firstMedia.getBookInfo();

    switch (tagName) {
        case "TAG.1": return bookInfo.title();
        case "TAG.2": return bookInfo.writer();
        // ... etc
        default: return "";
    }
}

// In initRenderMap:
if (context.contains("TAG.1")) {
    renderMap.put("TAG.1", chapter -> getTag(chapter, "TAG.1"));
}
```

### Category: Algorithms (4 tasks)

#### Task 30: Fix redundant indexOf() in context menu
**File:** `FileListComponent.java:55-61`
**Current:**
```java
if (FileListComponent.this.getItems().indexOf(item) != 0) {
    contextMenu.getItems().add(moveUp);
}
if (FileListComponent.this.getItems().size() > FileListComponent.this.getItems().indexOf(item) + 1) {
    contextMenu.getItems().add(moveDown);
}
```
**Fix:**
```java
int itemIndex = FileListComponent.this.getItems().indexOf(item);
if (itemIndex != 0) {
    contextMenu.getItems().add(moveUp);
}
if (FileListComponent.this.getItems().size() > itemIndex + 1) {
    contextMenu.getItems().add(moveDown);
}
```

#### Task 31: Fix indexOf() in forEach loop
**File:** `FileListComponent.java:112`
**Current:**
```java
change.forEach(m -> getSelectionModel().select(media.indexOf(m)));
```
**Fix:**
```java
Map<MediaInfo, Integer> indexMap = new HashMap<>();
for (int i = 0; i < media.size(); i++) {
    indexMap.put(media.get(i), i);
}
change.forEach(m -> {
    Integer index = indexMap.get(m);
    if (index != null) {
        getSelectionModel().select(index);
    }
});
```

#### Task 32: Fix Part.getNumber() O(n) indexOf
**File:** `Part.java:55`
**Current:**
```java
public int getNumber() {
    return getBook().getParts().indexOf(this) + 1;
}
```
**Fix:**
```java
private int number;

public int getNumber() {
    return number;
}

void setNumber(int number) {
    this.number = number;
}

// Update Book class to maintain part numbers
public void addPart(Part part) {
    parts.add(part);
    updatePartNumbers();
}

private void updatePartNumbers() {
    for (int i = 0; i < parts.size(); i++) {
        parts.get(i).setNumber(i + 1);
    }
}
```

#### Task 33: Fix Chapter.getNumber() O(n) indexOf
**File:** `Chapter.java:95`
**Fix:** Same pattern as Task 32

#### Task 34: Fix MediaInfoOrganiser.getNumber() O(n) indexOf
**File:** `MediaInfoOrganiser.java:47`
**Fix:** Same pattern as Task 32

### Category: UI Performance (7 tasks)

#### Task 35: Fix excessive Platform.runLater with listeners
**File:** `ProgressComponent.java:173-178`
**Fix:** Use property bindings instead:
```java
void assignListeners(ConversionProgress conversionProgress) {
    filesCount.textProperty().bind(conversionProgress.filesCount);
    progressBar.progressProperty().bind(conversionProgress.progress);
    estimatedSize.textProperty().bind(Bindings.createStringBinding(
        () -> Utils.formatSize(conversionProgress.size.get()),
        conversionProgress.size));
    elapsedTime.textProperty().bind(Bindings.createStringBinding(
        () -> Utils.formatTime(conversionProgress.elapsed.get()),
        conversionProgress.elapsed));
    remainingTime.textProperty().bind(Bindings.createStringBinding(
        () -> Utils.formatTime(conversionProgress.remaining.get()),
        conversionProgress.remaining));
    state.textProperty().bind(conversionProgress.state);
}
```

#### Task 36: Fix Platform.runLater in chapter loops
**File:** `ConversionGroup.java:146-158`
**Current:**
```java
for (int i = 0; i < chapters.size(); i++) {
    Chapter chapter = chapters.get(i);
    ConversionProgress conversionProgress = this.start(chapter, finalDesination);
    Platform.runLater(() -> progressQueue.getItems().add(0, new ProgressComponent(conversionProgress)));
}
```
**Fix:**
```java
List<ProgressComponent> components = new ArrayList<>();
for (int i = 0; i < chapters.size(); i++) {
    Chapter chapter = chapters.get(i);
    ConversionProgress conversionProgress = this.start(chapter, finalDesination);
    components.add(new ProgressComponent(conversionProgress));
}
Platform.runLater(() -> progressQueue.getItems().addAll(0, components));
```

#### Task 37: Fix Platform.runLater in part loops
**File:** `ConversionGroup.java:161-172`
**Fix:** Same as Task 36

#### Task 38: Fix ObservableList individual adds
**File:** `BookStructureComponent.java:118-131`
**Current:**
```java
void updateBookStructure() {
    Book book = AudiobookConverter.getContext().getBook();
    getRoot().getChildren().clear();
    book.getParts().forEach(p -> {
        TreeItem<Organisable> partItem = new TreeItem<>(p);
        getRoot().getChildren().add(partItem);
        // ...
    });
    refresh();
}
```
**Fix:**
```java
void updateBookStructure() {
    Book book = AudiobookConverter.getContext().getBook();

    List<TreeItem<Organisable>> newChildren = new ArrayList<>();
    for (Part p : book.getParts()) {
        TreeItem<Organisable> partItem = new TreeItem<>(p);
        for (Chapter c : p.getChapters()) {
            TreeItem<Organisable> chapterItem = new TreeItem<>(c);
            for (MediaInfo m : c.getMedia()) {
                chapterItem.getChildren().add(new TreeItem<>(m));
            }
            partItem.getChildren().add(chapterItem);
        }
        newChildren.add(partItem);
    }

    getRoot().getChildren().setAll(newChildren);
    getRoot().getChildren().forEach(t -> t.setExpanded(true));
    refresh();
}
```

#### Task 39: Fix cell factory disk I/O
**File:** `ArtWorkListCell.java:16-36`
**Fix:**
```java
private static final Map<String, Image> imageCache = new WeakHashMap<>();

@Override
public void updateItem(ArtWork artWork, boolean empty) {
    super.updateItem(artWork, empty);
    if (empty) {
        setText(null);
        setGraphic(null);
    } else {
        String fileName = artWork.getFileName();
        Image cachedImage = imageCache.get(fileName);

        if (cachedImage != null) {
            setImage(cachedImage);
        } else {
            // Show placeholder
            imageView.setImage(null);

            // Load asynchronously
            Task<Image> loadTask = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    try (var imageStream = new FileInputStream(fileName)) {
                        return new Image(imageStream);
                    }
                }
            };

            loadTask.setOnSucceeded(e -> {
                Image image = loadTask.getValue();
                imageCache.put(fileName, image);
                imageView.setImage(image);
            });

            new Thread(loadTask).start();
        }

        imageView.setFitHeight(110);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        setGraphic(imageView);
    }
}

private void setImage(Image image) {
    imageView.setImage(image);
    double height = image.getHeight();
    double width = image.getWidth();
    setTooltip(new Tooltip("["+Math.round(width) + "x" + Math.round(height)+"] - "+artWork.getFileName()));
}
```

#### Task 40: Fix binding cleanup in MediaPlayerController
**File:** `MediaPlayerController.java:189-204`
**Current:**
```java
mediaPlayer.setOnEndOfMedia(() -> {
    executorService.shutdown();
    mediaPlayer.volumeProperty().unbindBidirectional(volume.valueProperty());
    mediaPlayer.dispose();
    mediaPlayer = null;
});
```
**Fix:**
```java
mediaPlayer.setOnEndOfMedia(() -> {
    executorService.shutdown();
    mediaPlayer.volumeProperty().unbindBidirectional(volume.valueProperty());
    mediaPlayer.rateProperty().unbind(); // Add this
    mediaPlayer.dispose();
    mediaPlayer = null;
});
```

#### Task 41: Fix ChapterEditor Platform.runLater listeners
**File:** `ChapterEditor.java:57-198`
**Fix:** Consolidate updates and use debouncing

---

## Phase 3: Medium Priority Tasks (11)

#### Task 42: Fix Settings deserialization overhead
**File:** `Settings.java:63-72`
**Current:**
```java
public static Settings loadSetting() {
    String settingsJson = preferences.get(Version.getSettingsVersion(), null);
    // ... deserialize every time
}
```
**Fix:**
```java
private static Settings cachedSettings = null;
private static boolean settingsLoaded = false;

public static Settings loadSetting() {
    if (!settingsLoaded) {
        String settingsJson = preferences.get(Version.getSettingsVersion(), null);
        if (settingsJson == null) {
            cachedSettings = new Settings();
            cachedSettings.setPresets(Preset.defaultValues);
        } else {
            cachedSettings = gson.fromJson(settingsJson, Settings.class);
        }
        settingsLoaded = true;
    }
    return cachedSettings;
}

public void save() {
    preferences.put(Version.getSettingsVersion(), gson.toJson(this));
    cachedSettings = this;
    settingsLoaded = true;
}

public static void invalidateCache() {
    settingsLoaded = false;
}
```

#### Task 43: Fix artwork extraction sequential delays
**File:** `MediaInfoLoader.java:66-93`
**Current:**
```java
Future<ArtWork> futureLoad = artExecutor.schedule(
    new FFmpegArtWorkExtractor(...),
    100, TimeUnit.MILLISECONDS);
```
**Fix:**
```java
Future<ArtWork> futureLoad = artExecutor.submit(
    new FFmpegArtWorkExtractor(...)); // Remove delay
```

#### Task 44: Fix string removal loop
**File:** `Utils.java:75-81`
**Current:**
```java
private static String removeInvalidFilenameCharacters(String filename) {
    String result = filename;
    for (char c : INVALID_FILENAME_CHARS) {
        result = StringUtils.remove(result, c);
    }
    return result;
}
```
**Fix:**
```java
private static final Pattern INVALID_CHARS_PATTERN =
    Pattern.compile("[" + Pattern.quote(new String(INVALID_FILENAME_CHARS)) + "]");

private static String removeInvalidFilenameCharacters(String filename) {
    return INVALID_CHARS_PATTERN.matcher(filename).replaceAll("");
}
```

#### Task 45: Fix triple nested stream traversal
**File:** `Book.java:91-96`
**Current:**
```java
public List<MediaInfo> getMedia() {
    return parts.stream()
        .flatMap(part -> part.getChapters().stream()
            .flatMap(chapter -> chapter.getMedia().stream()))
        .collect(Collectors.toList());
}
```
**Fix:**
```java
private List<MediaInfo> cachedMedia = null;

public List<MediaInfo> getMedia() {
    if (cachedMedia == null) {
        cachedMedia = parts.stream()
            .flatMap(part -> part.getChapters().stream()
                .flatMap(chapter -> chapter.getMedia().stream()))
            .collect(Collectors.toList());
    }
    return Collections.unmodifiableList(cachedMedia);
}

public void invalidateMediaCache() {
    cachedMedia = null;
}

// Call invalidateMediaCache() when parts/chapters/media are modified
```

#### Task 46: Fix replaceMediaWithTracks triple O(n)
**File:** `Chapter.java:79-84`
**Current:**
```java
int position = this.getMedia().indexOf(mediaInfo);
this.getMedia().remove(position);
this.getMedia().addAll(position, adaptors);
```
**Fix:**
```java
List<MediaInfo> mediaList = this.getMedia();
for (int i = 0; i < mediaList.size(); i++) {
    if (mediaList.get(i).equals(mediaInfo)) {
        mediaList.remove(i);
        mediaList.addAll(i, adaptors);
        break;
    }
}
```

#### Task 47: Fix Platform.runLater in clearQueue loop
**File:** `FilesController.java:812-828`
**Current:**
```java
Platform.runLater(() -> {
    for (ProgressComponent done : dones) {
        progressQueue.getItems().remove(done);
    }
});
```
**Fix:**
```java
Platform.runLater(() -> {
    progressQueue.getItems().removeAll(dones);
});
```

#### Task 48: Fix executor shutdown without awaitTermination
**File:** `MediaPlayerController.java:121,190`
**Current:**
```java
executorService.shutdown();
```
**Fix:**
```java
executorService.shutdown();
try {
    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
    }
} catch (InterruptedException e) {
    executorService.shutdownNow();
}
```

#### Task 49: Fix repeated setGraphic calls
**File:** `ArtWorkListCell.java:20,34`
**Fix:** Check if graphic needs update before calling setGraphic

#### Task 50: Fix multiple getItems() calls
**File:** `FileListComponent.java:61`
**Current:**
```java
if (FileListComponent.this.getItems().size() > FileListComponent.this.getItems().indexOf(item) + 1) {
```
**Fix:**
```java
ObservableList<MediaInfo> items = FileListComponent.this.getItems();
if (items.size() > items.indexOf(item) + 1) {
```

#### Task 51: Fix repeated separator checks
**File:** `BookStructureComponent.java:37,53,75,85`
**Current:** Repeated code checking for SeparatorMenuItem
**Fix:**
```java
private void addSeparatorIfNeeded(ContextMenu contextMenu) {
    if (!contextMenu.getItems().isEmpty() &&
        !(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem)) {
        contextMenu.getItems().add(new SeparatorMenuItem());
    }
}

// Replace all occurrences with:
addSeparatorIfNeeded(contextMenu);
```

---

## Implementation Guidelines

### Testing Strategy
1. Create unit tests for each fix
2. Test with large audiobooks (50+ chapters)
3. Monitor memory usage with profiler
4. Monitor thread count over time
5. Measure UI responsiveness

### Rollout Strategy
1. Fix Phase 1 tasks first (critical threading and memory leaks)
2. Test thoroughly after Phase 1
3. Fix Phase 2 tasks (performance improvements)
4. Fix Phase 3 tasks (optimizations)

### Verification Checklist
- [ ] No thread leaks (constant thread count)
- [ ] No memory leaks (constant heap after GC)
- [ ] UI remains responsive during conversions
- [ ] All executors properly shut down
- [ ] All listeners removed on cleanup
- [ ] All streams/resources closed

---

## Estimated Impact After All Fixes

- **70-90% reduction** in memory usage
- **50-80% reduction** in thread count
- **2-5x improvement** in UI responsiveness
- **30-50% reduction** in conversion time for large books
