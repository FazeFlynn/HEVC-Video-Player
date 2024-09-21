import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.media.TrackType;
// import uk.co.caprica.vlcj.media.track.AudioTrack;
// import uk.co.caprica.vlcj.media.track.SubtitleTrack;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

public class MyPlayer {
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JFrame frame;
    private JComboBox<String> audioTrackBox;
    private JComboBox<String> subtitleTrackBox;
    private JList<String> videoList;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyPlayer().initUI();
        });
    }

    // test dir
    String dir = "C:/Users/user/Downloads";


    public void initUI() {
        frame = new JFrame("Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(800, 600);

        // drag-and-drop functionality
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File droppedFile = droppedFiles.get(0);
                        if (isVideoFile(droppedFile)) {
                            playVideoFile(droppedFile);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Please give valid video file.");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.add(mediaPlayerComponent, BorderLayout.CENTER);

        // control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        // audioBox
        audioTrackBox = new JComboBox<>();
        audioTrackBox.addActionListener(e -> changeAudioTrack());
        controlPanel.add(new JLabel("Audio Track: "), BorderLayout.WEST);
        controlPanel.add(audioTrackBox, BorderLayout.CENTER);

        // subBox
        subtitleTrackBox = new JComboBox<>();
        subtitleTrackBox.addActionListener(e -> changeSubtitleTrack());
        controlPanel.add(new JLabel("Subtitle Track: "), BorderLayout.EAST);
        controlPanel.add(subtitleTrackBox, BorderLayout.SOUTH);

        frame.add(controlPanel, BorderLayout.SOUTH);

        // File list panel
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());

        // Listing video files form the test dir
        File directory = new File(dir);
        String[] videoFiles = directory.list((dir, name) -> name.endsWith(".mp4") || name.endsWith(".mkv"));
        
        if (videoFiles != null) {
            videoList = new JList<>(videoFiles);
            videoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            videoList.addListSelectionListener(e -> playSelectedVideo(videoFiles[videoList.getSelectedIndex()]));
            filePanel.add(new JScrollPane(videoList), BorderLayout.CENTER);
        } else {
            filePanel.add(new JLabel("No videos files found."), BorderLayout.CENTER);
        }

        frame.add(filePanel, BorderLayout.WEST);
        frame.setVisible(true);
    }

    // Play from the list
    private void playSelectedVideo(String fileName) {
        File file = new File(dir + fileName); // Update path as needed
        playVideoFile(file);
    }

    // play
    private void playVideoFile(File file) {
        mediaPlayerComponent.mediaPlayer().media().play(file.getAbsolutePath());

        populateTracks();
    }

    // validating with extensions
    private boolean isVideoFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".avi");
    }

    // change audio track
    private void changeAudioTrack() {
        int selectedTrackIndex = audioTrackBox.getSelectedIndex();
        if (selectedTrackIndex >= 0) {
            mediaPlayerComponent.mediaPlayer().audio().setTrack(selectedTrackIndex);
        }
    }

    // change subtitle track
    private void changeSubtitleTrack() {
        int selectedTrackIndex = subtitleTrackBox.getSelectedIndex();
        if (selectedTrackIndex >= 0) {
            mediaPlayerComponent.mediaPlayer().subpictures().setTrack(selectedTrackIndex);
        }
    }

    // show audio and subtitle names
    private void populateTracks() {
        audioTrackBox.removeAllItems();
        List<uk.co.caprica.vlcj.media.track.AudioTrack> audioTracks = mediaPlayerComponent.mediaPlayer().media().tracks().all(TrackType.AUDIO);
        for (uk.co.caprica.vlcj.media.track.AudioTrack track : audioTracks) {
            audioTrackBox.addItem(track.description());
        }

        // subtitle tracks
        subtitleTrackBox.removeAllItems();
        List<uk.co.caprica.vlcj.media.track.SubtitleTrack> subtitleTracks = mediaPlayerComponent.mediaPlayer().media().tracks().all(TrackType.TEXT);
        for (uk.co.caprica.vlcj.media.track.SubtitleTrack track : subtitleTracks) {
            subtitleTrackBox.addItem(track.description());
        }
    }
}
