const {chromium}=require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
(async()=>{
 const browser=await chromium.launch({channel:process.env.BROWSER_CHANNEL || 'chrome',headless:true});
 const evidence=process.env.EVIDENCE_DIR || '../docs/integracion/evidencias';
 const origin=process.env.CUENTIX_TEST_ORIGIN || 'http://127.0.0.1:4200';
 try {
 fs.mkdirSync(evidence,{recursive:true});
 for(const width of [390,1440]){
 const page=await browser.newPage({viewport:{width,height:900},hasTouch:width===390});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.route('**/api/**',async route=>{
 const path=new URL(route.request().url()).pathname;
 const profile={id:12,codigo:'11223344',rolId:2,estado:true,nombre:'Ana',apellido:'Perez',telefono:'999123456',csrfToken:'ui-test-token'};
 if(path==='/api/auth/session'||path==='/api/auth/login')return route.fulfill({json:profile});
 if(path==='/api/cliente/mis-compras')return route.fulfill({json:[]});
 if(path==='/api/cliente/mis-acuerdos')return route.fulfill({json:[{id:1,tienda:'Tienda de prueba',tasa:12,tipoTasa:'TEA',linea:1000,moneda:'S/',meses:12,diaCorte:20,diaPago:25}]});
 return route.fulfill({json:{},status:404});
 });
 await page.goto(origin+'/auth/login');
 const help=page.getByRole('button',{name:'Ayuda para Usuario, DNI o RUC'});
 const tooltip=page.locator('app-field-help [popover]:popover-open');
 if(width===390) await help.tap();
 else {
   await page.getByLabel('Usuario, DNI o RUC',{exact:true}).focus();
   await page.keyboard.press('Shift+Tab');
 }
 await tooltip.waitFor({state:'visible'});
 assert.match(await tooltip.innerText(),/Escribe el usuario/);
 if(width===390) await help.tap();
 else await page.keyboard.press('Escape');
 await tooltip.waitFor({state:'hidden'});
 await page.getByRole('button',{name:'Ingresar',exact:true}).click();
 await page.getByRole('alert').waitFor();
 assert.match(await page.getByRole('alert').innerText(),/Complete/);
 await page.screenshot({path:path.join(evidence,'login-'+width+'.png'),fullPage:true});
 await page.getByLabel('Usuario, DNI o RUC',{exact:true}).fill('11223344');
 await page.getByLabel('Contraseña',{exact:true}).fill('Password123');
 await page.getByRole('button',{name:'Ingresar',exact:true}).click();
 await page.getByRole('heading',{name:'Reporte de compras'}).waitFor();
 await page.getByText('No tienes compras registradas.').waitFor();
 await page.getByRole('link',{name:'Acuerdos',exact:true}).click();
 await page.getByRole('link',{name:'Detalle',exact:true}).click();
 await page.getByRole('heading',{name:'Detalle del acuerdo'}).waitFor();
 await page.getByText('Tienda de prueba',{exact:true}).waitFor();
 await page.screenshot({path:path.join(evidence,'acuerdo-'+width+'.png'),fullPage:true});
 assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,'Horizontal overflow at '+width);
 assert.deepEqual(errors,[]);
 await page.close();
 console.log('PASS browser UI at '+width+'px (HTTP fixtures; backend tested separately)');
 }
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exit(1);});
