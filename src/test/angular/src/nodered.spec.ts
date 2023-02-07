import {baseUrl} from './page/page';
import {settings} from './fixture/settings/settings';
import {testSpeed} from './shared/helper';
import {StatusPage} from './page/status.page';

fixture('Node Red')
    .beforeEach(async t => {
        await t.maximizeWindow();
        await t.setTestSpeed(testSpeed);
    })
    .page(baseUrl());

test('Export flows', async t => {
    await StatusPage.openFlowExportDialog(t);
    await StatusPage.assertFlowExportDialogContent(t);
    await StatusPage.closeFlowExportDialog(t);
});
