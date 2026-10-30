
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:image_picker/image_picker.dart';
import 'package:electorama_app/infrastructure/services/camera_gallery_service.dart';
import 'package:path_provider/path_provider.dart';



class CameraGalleryServiceImpl extends CameraGalleryService{
  final ImagePicker _picker = ImagePicker();


  @override
  Future<String?> selectPhoto() async {
    final XFile? photo = await _picker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 80,
    );
    if(photo ==null) return null;

    // File file = File(photo.path);

    // print('tenemos una imagen ${photo.path}');
    // var response = compressAndGetFile(file);
    
    return photo.path;
  }

  @override
  Future<String?> takePhoto() async {
    final XFile? photo = await _picker.pickImage(
      source: ImageSource.camera,
      imageQuality: 80,
      preferredCameraDevice: CameraDevice.rear
    );
    if(photo ==null) return null;
    //File file = File(photo.path);

    //print('tenemos una imagen ${photo.path}');
    //var response = compressAndGetFile(file);
    
    return photo.path;
  }
  
  @override
  Future<Uint8List?> compressAndGetFile(File file) async{
    var result = await FlutterImageCompress.compressWithFile(
      file.absolute.path,
      minWidth: 800,
      quality: 20,
    );
    if(result ==null) return null;

    return result;
    
  }
  Future<void> deleteCacheDir() async {
    final cacheDir = await getTemporaryDirectory();

    if (cacheDir.existsSync()) {
      cacheDir.deleteSync(recursive: true);
    }
  }

}