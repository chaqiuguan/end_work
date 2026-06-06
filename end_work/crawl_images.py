# -*- coding: utf-8 -*-
"""DuckDuckGo图片搜索 → 替换picsum URL"""
import subprocess, json, time, sys
from duckduckgo_search import DDGS

def run_sql(sql):
    p = 'D:/NEUwork/web/p/_tmp.sql'
    open(p, 'w', encoding='utf-8').write(sql)
    subprocess.run(['docker','cp',p,'zhuanbaomao-mysql:/tmp/_tmp.sql'], capture_output=True)
    subprocess.run(['docker','exec','zhuanbaomao-mysql','mysql','-u','root','-proot123456','--default-character-set=utf8mb4','zhuanbaomao','-e','source /tmp/_tmp.sql'], capture_output=True)

r = subprocess.run(['docker','exec','zhuanbaomao-mysql','mysql','-u','root','-proot123456','--default-character-set=utf8mb4','zhuanbaomao','-N','-e',
    "SELECT id,title FROM product WHERE deleted=0 AND images LIKE '%picsum%' ORDER BY id"], capture_output=True)
products = []
for l in r.stdout.decode('utf-8').strip().split('\n'):
    p = l.strip().split('\t')
    if len(p)>=2: products.append((int(p[0]), p[1]))
print(f'待处理: {len(products)} 件')

ddgs = DDGS()
done = skipped = 0
for pid, title in products:
    try:
        kw = ' '.join(title.split()[:4])
        imgs = []
        for r in ddgs.images(kw, max_results=15):
            u = r.get('image','')
            if u and u.startswith('https://') and not u.endswith('.svg'):
                imgs.append(u)
            if len(imgs) >= 3: break
        if not imgs:
            skipped += 1; time.sleep(0.3); continue
        j = json.dumps(imgs, ensure_ascii=False).replace("'","''")
        run_sql(f"UPDATE product SET images = '{j}' WHERE id = {pid};")
        done += 1
        if done % 15 == 0: print(f'{done}/{len(products)}')
        time.sleep(0.5)
    except Exception as e:
        skipped += 1; time.sleep(1)
print(f'\n完成: {done} 更新, {skipped} 跳过')
