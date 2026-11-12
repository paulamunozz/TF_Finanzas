import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Client } from '../../core/models/client.model';

@Component({
  selector: 'app-delete-client-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './delete-client-dialog.html',
  styleUrl: './delete-client-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeleteClientDialog {
  constructor(
    public readonly ref: MatDialogRef<DeleteClientDialog>,
    @Inject(MAT_DIALOG_DATA) public readonly data: Client,
  ) {}
}
