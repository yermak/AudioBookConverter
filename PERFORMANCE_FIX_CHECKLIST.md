# Performance Fix Checklist

Quick reference for fixing performance issues. Check off items as you complete them.

---

## Phase 1: Critical Fixes (Do First!)

### Threading - ExecutorService Shutdown (5 tasks)

- [ ] **Task 1:** `StreamCopier.java:23` - Add shutdown hook
- [ ] **Task 2:** `ConversionJob.java:29` - Add shutdown hook
- [ ] **Task 3:** `FFMediaLoader.java:35` - Add shutdown hook
- [ ] **Task 4:** `MediaInfoLoader.java:40` - Add shutdown hook
- [ ] **Task 5:** `ConversionContext.java:49` - Add shutdown hook

**Implementation Pattern:**
```java
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
```

### Threading - Remove Ad-hoc Executors (5 tasks)

- [ ] **Task 6:** `AudiobookConverter.java:73` - Reuse shared executor
- [ ] **Task 7:** `ConversionGroup.java:50` - Reuse shared executor
- [ ] **Task 8:** `FilesController.java:656` - Reuse shared executor
- [ ] **Task 9:** `FilesController.java:713` - Reuse shared executor
- [ ] **Task 10:** `BookInfoController.java:152` - Reuse shared executor

**Pattern:** Replace `Executors.newSingleThreadExecutor().submit(...)` with `ConversionContext.getExecutorService().submit(...)`

### Threading - Fix Busy-Wait Loops (5 tasks)

- [ ] **Task 11:** `FFMpegNativeConverter.java:41` - Use CountDownLatch
- [ ] **Task 12:** `FFMpegConcatenator.java:54` - Use CountDownLatch
- [ ] **Task 13:** `FFMpegOptimizer.java:38` - Use CountDownLatch
- [ ] **Task 14:** `AudiobookConverter.java:119-128` - Proper shutdown sequence
- [ ] **Task 15:** `FilesController.java:800-809` - Proper shutdown sequence

**Pattern:** Replace `while (paused) Thread.sleep(1000)` with `pauseLatch.await()`

### Memory - Resource Leaks (4 tasks)

- [ ] **Task 16:** `FlacPicture.java:24-25` - Use try-with-resources
- [ ] **Task 23:** `ConversionContext.java:35` - Add job cleanup
- [ ] **Task 24:** `ConversionContext.java:52` - Use WeakHashMap
- [ ] **Task 25:** `ConversionProgress.java:95-124` - Fix synchronization

### Memory - Listener Cleanup (6 tasks)

- [ ] **Task 17:** `ProgressComponent.java:173-178` - Store and remove listeners
- [ ] **Task 18:** `BookInfoController.java:128-142` - Store and remove listeners
- [ ] **Task 19:** `FilesController.java:410-434` - Store and remove listeners
- [ ] **Task 20:** `OutputController.java` - Store and remove listeners
- [ ] **Task 21:** `FileListComponent.java:20-23` - Store and remove listeners
- [ ] **Task 22:** `ConversionContext.java:125-174` - Add remove methods

**Pattern:**
```java
private final List<ChangeListener<?>> listeners = new ArrayList<>();

void addListener() {
    ChangeListener<String> listener = (obs, old, new) -> { ... };
    property.addListener(listener);
    listeners.add(listener);
}

public void cleanup() {
    // Remove all listeners
    listeners.clear();
}
```

---

## Phase 2: High Priority Fixes

### N+1 Patterns (4 tasks)

- [ ] **Task 26:** `DurationVerifier.java:24` - FFprobe singleton
- [ ] **Task 27:** `FFMediaLoader.java:74-104` - Optimize directory scan
- [ ] **Task 28:** `FlacLoader.java:26-59` - Cache tracks reference
- [ ] **Task 29:** `Chapter.java:29-77` - Extract media.get(0) once

### Algorithms - indexOf() Optimization (5 tasks)

- [ ] **Task 30:** `FileListComponent.java:55-61` - Cache indexOf result
- [ ] **Task 31:** `FileListComponent.java:112` - Build index map
- [ ] **Task 32:** `Part.java:55` - Track position as field
- [ ] **Task 33:** `Chapter.java:95` - Track position as field
- [ ] **Task 34:** `MediaInfoOrganiser.java:47` - Track position as field

### UI Performance (7 tasks)

- [ ] **Task 35:** `ProgressComponent.java:173-178` - Use property bindings
- [ ] **Task 36:** `ConversionGroup.java:146-158` - Batch Platform.runLater
- [ ] **Task 37:** `ConversionGroup.java:161-172` - Batch Platform.runLater
- [ ] **Task 38:** `BookStructureComponent.java:118-131` - Use setAll()
- [ ] **Task 39:** `ArtWorkListCell.java:16-36` - Async image loading + cache
- [ ] **Task 40:** `MediaPlayerController.java:189-204` - Unbind rate property
- [ ] **Task 41:** `ChapterEditor.java:57-198` - Consolidate updates

---

## Phase 3: Medium Priority Optimizations

### Caching (4 tasks)

- [ ] **Task 42:** `Settings.java:63-72` - Cache Settings instance
- [ ] **Task 43:** `MediaInfoLoader.java:66-93` - Remove artificial delays
- [ ] **Task 44:** `Utils.java:75-81` - Use regex instead of loop
- [ ] **Task 45:** `Book.java:91-96` - Cache media list

### Micro-optimizations (7 tasks)

- [ ] **Task 46:** `Chapter.java:79-84` - Optimize replaceMediaWithTracks
- [ ] **Task 47:** `FilesController.java:812-828` - Use removeAll()
- [ ] **Task 48:** `MediaPlayerController.java:121,190` - Add awaitTermination
- [ ] **Task 49:** `ArtWorkListCell.java:20,34` - Check before setGraphic
- [ ] **Task 50:** `FileListComponent.java:61` - Cache getItems()
- [ ] **Task 51:** `BookStructureComponent.java:37,53,75,85` - Extract helper method

---

## Testing Checklist

After each phase, verify:

### Memory Tests
- [ ] Run 10 consecutive conversions
- [ ] Check heap usage stays constant after GC
- [ ] Verify no file descriptor leaks (`lsof` on Linux/Mac)
- [ ] Profile with VisualVM or JProfiler

### Threading Tests
- [ ] Monitor thread count during conversions
- [ ] Verify thread count returns to baseline after conversions
- [ ] Check all executors shut down on app close
- [ ] Test pause/resume functionality

### Performance Tests
- [ ] Convert large audiobook (50+ chapters)
- [ ] Measure total conversion time
- [ ] Monitor UI responsiveness during conversion
- [ ] Test artwork list scrolling smoothness
- [ ] Verify Settings access is fast

### Functional Tests
- [ ] All conversion features still work
- [ ] UI updates correctly during conversion
- [ ] Progress bars update smoothly
- [ ] Chapter editor works correctly
- [ ] Media player functions properly

---

## Quick Reference: Common Patterns

### Pattern 1: Fix ExecutorService Leak
```java
// Add static shutdown method
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

// Call from app shutdown
```

### Pattern 2: Fix Listener Leak
```java
// Store listeners
private final List<ChangeListener<?>> listeners = new ArrayList<>();

// Add with storage
ChangeListener<T> listener = (obs, old, val) -> { ... };
property.addListener(listener);
listeners.add(listener);

// Remove in cleanup
public void cleanup() {
    listeners.forEach(property::removeListener);
    listeners.clear();
}
```

### Pattern 3: Fix Platform.runLater in Loop
```java
// BEFORE (bad)
for (Item item : items) {
    Platform.runLater(() -> list.add(new Component(item)));
}

// AFTER (good)
List<Component> components = new ArrayList<>();
for (Item item : items) {
    components.add(new Component(item));
}
Platform.runLater(() -> list.addAll(components));
```

### Pattern 4: Fix Resource Leak
```java
// BEFORE (bad)
FileInputStream stream = new FileInputStream(file);
Image image = new Image(stream);

// AFTER (good)
try (FileInputStream stream = new FileInputStream(file)) {
    Image image = new Image(stream);
}
```

### Pattern 5: Cache Expensive Operations
```java
// BEFORE (bad)
public Settings loadSetting() {
    String json = preferences.get(...);
    return gson.fromJson(json, Settings.class);
}

// AFTER (good)
private static Settings cached = null;
public Settings loadSetting() {
    if (cached == null) {
        String json = preferences.get(...);
        cached = gson.fromJson(json, Settings.class);
    }
    return cached;
}
```

### Pattern 6: Batch ObservableList Updates
```java
// BEFORE (bad)
list.clear();
items.forEach(item -> list.add(item));

// AFTER (good)
list.setAll(items);
```

---

## Metrics to Track

### Before Fixes (Baseline)
- [ ] Record: Thread count at startup: ___
- [ ] Record: Thread count after 10 conversions: ___
- [ ] Record: Heap usage after 10 conversions: ___ MB
- [ ] Record: Time to convert 50-chapter book: ___ seconds
- [ ] Record: UI freeze duration during conversion: ___ seconds

### After Phase 1
- [ ] Thread count at startup: ___
- [ ] Thread count after 10 conversions: ___
- [ ] Heap usage after 10 conversions: ___ MB
- [ ] Improvement: ___%

### After Phase 2
- [ ] Time to convert 50-chapter book: ___ seconds
- [ ] UI freeze duration: ___ seconds
- [ ] Improvement: ___%

### After Phase 3
- [ ] Overall memory reduction: ___%
- [ ] Overall speed improvement: ___%
- [ ] Thread leak fixed: ✓/✗
- [ ] Listener leak fixed: ✓/✗

---

## Common Pitfalls to Avoid

❌ **Don't:**
- Skip listener removal (causes memory leaks)
- Create executors without shutdown hooks
- Use `Thread.sleep()` for synchronization
- Perform I/O operations in cell factories
- Call `Platform.runLater()` in loops
- Access collections without synchronization from multiple threads
- Leave streams/resources unclosed

✅ **Do:**
- Always remove listeners in cleanup methods
- Reuse shared executor services
- Use CountDownLatch/Condition for waiting
- Load images asynchronously with caching
- Batch UI updates into single Platform.runLater
- Use ConcurrentHashMap or synchronized blocks
- Use try-with-resources for AutoCloseable

---

## Progress Tracking

**Phase 1 Completion:** ☐ 0/25 tasks (___%)
**Phase 2 Completion:** ☐ 0/15 tasks (___%)
**Phase 3 Completion:** ☐ 0/11 tasks (___%)

**Overall Progress:** ☐ 0/51 tasks (___%)

**Estimated Completion Date:** _______________

**Target Improvements:**
- [x] 70%+ memory reduction
- [x] 50%+ thread reduction
- [x] 2x+ UI responsiveness
- [x] 30%+ faster conversions
