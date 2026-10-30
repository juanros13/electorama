import 'package:electorama_app/domain/entities/visits/visit_post.dart';

abstract class VisitPostDataSource {

  Future<List<VisitPost>> getAllVisits();
  

}