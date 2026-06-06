import os, re

base = os.path.join('microservices')

# Fix all modules
for module in ['common', 'user-service', 'product-service', 'order-service', 'gateway']:
    mod_path = os.path.join(base, module, 'src', 'main', 'java', 'com', 'zhuanbaomao')
    if not os.path.exists(mod_path):
        print(f'Skip {module}: no source')
        continue

    for root, dirs, files in os.walk(mod_path):
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(root, f)
            content = open(path, encoding='utf-8').read()

            # Determine correct package from file path
            rel = os.path.relpath(root, mod_path).replace(os.sep, '/')
            if rel == '.':
                pkg = 'com.zhuanbaomao'
            else:
                pkg = 'com.zhuanbaomao.' + rel.replace('/', '.')

            # Fix package declaration
            old_pkg_m = re.search(r'^package\s+([\w.]+)\s*;', content, re.MULTILINE)
            if old_pkg_m:
                old_pkg = old_pkg_m.group(1)
                if old_pkg != pkg:
                    # Update package
                    content = content.replace(f'package {old_pkg};', f'package {pkg};', 1)
                    # Update same-package imports
                    content = content.replace(f'import {old_pkg}.', f'import {pkg}.')
                    open(path, 'w', encoding='utf-8').write(content)
                    print(f'  {module}: {f} {old_pkg} -> {pkg}')

print('All packages fixed')
