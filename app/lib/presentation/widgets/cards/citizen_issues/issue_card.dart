import 'package:flutter/material.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';

class IssueCard extends StatelessWidget {
  final Issue issue;
  const IssueCard({super.key, required this.issue});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    final urgent = issue.details.any((d) => d.urgent);
    final locality = (issue.localityOther ?? '').isNotEmpty
        ? ' · ${issue.localityOther}'
        : '';

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(issue.fullName,
                      style: textTheme.titleMedium
                          ?.copyWith(fontWeight: FontWeight.w700)),
                ),
                if (urgent)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.danger.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text('URGENTE',
                        style: textTheme.labelSmall
                            ?.copyWith(color: AppColors.danger)),
                  ),
              ],
            ),
            const SizedBox(height: 2),
            Text('${issue.municipalityName}$locality',
                style: textTheme.bodySmall
                    ?.copyWith(color: colors.onSurfaceVariant)),
            Text('Tel: ${issue.phone}',
                style: textTheme.bodySmall
                    ?.copyWith(color: colors.onSurfaceVariant)),
            const SizedBox(height: AppSpacing.sm),
            Wrap(
              spacing: 6,
              runSpacing: 4,
              children: issue.details
                  .map((d) => Chip(
                        label: Text(d.issueCategoryName),
                        visualDensity: VisualDensity.compact,
                        materialTapTargetSize:
                            MaterialTapTargetSize.shrinkWrap,
                      ))
                  .toList(),
            ),
          ],
        ),
      ),
    );
  }
}
