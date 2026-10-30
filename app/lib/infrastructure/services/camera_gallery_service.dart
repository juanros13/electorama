import 'dart:io';
import 'dart:typed_data';

abstract class CameraGalleryService{
  Future<String?> takePhoto();
  Future<String?> selectPhoto();
  Future<Uint8List?> compressAndGetFile(File file);
}