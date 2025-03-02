function doDrag() {
    const container = document.querySelector('.container');
    const dragBar = document.querySelector('.drag-bar');
    const aside = document.querySelector('.aside');
    const content = document.querySelector('.content');
    const contentMinWidth = Number.parseInt(window.getComputedStyle(content).minWidth.replace('px', ''));
    const asideMinWidth = Number.parseInt(window.getComputedStyle(aside).minWidth.replace('px', ''));
    const dragBarWidth = dragBar.clientWidth;
    // 鼠标按下
    const onMouseDown = (e) => {
        // 鼠标开始的X轴坐标
        const startX = e.clientX;
        // 拖动块距离屏幕左侧的偏移量
        const offsetLeft = dragBar.offsetLeft;
        // 禁止文本选择（防止出现蓝色背景）
        document.body.style.userSelect = 'none';
        // 鼠标移动
        const onMouseMove = (e) => {
            // 拖动块的最大移动距离
            let maxWidth = container.clientWidth - contentMinWidth;
            // 鼠标箭头移动位置
            let pointerOffset = offsetLeft + (e.clientX - startX);
            // 边界值处理
            pointerOffset < asideMinWidth && (pointerOffset = asideMinWidth);
            pointerOffset > maxWidth && (pointerOffset = maxWidth);
            // 设置值（保证拖动块一直在拖动盒子的相对位置）
            aside.style.width = pointerOffset + 'px';
            dragBar.style.left = (pointerOffset === maxWidth) ? pointerOffset + 'px' : (pointerOffset - dragBarWidth / 2) + 'px';
            content.style.left = pointerOffset + 'px';
            content.style.width = (container.clientWidth - pointerOffset) + 'px';
            // 阻止默认行为，防止浏览器默认的文本选择等
            e.preventDefault();
        };
        // 鼠标松开
        const onMouseUp = () => {
            // 移除事件监听器
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            // 恢复文本选择
            document.body.style.userSelect = 'auto';
        };
        // 绑定鼠标移动和松开事件
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp, {once: true});
        // 防止文本选中和其他默认操作
        return false;
    };
    // 绑定鼠标按下事件
    dragBar.addEventListener('mousedown', onMouseDown);
}

function buildMenu() {
    let lastElement;
    const headings = document.querySelector(".content").querySelectorAll('h1, h2, h3, h4, h5, h6');
    const aside = document.querySelector(".aside");
    for (let i = 0; i < headings.length; i++) {
        const heading = headings[i];
        heading.id = "item" + i;
        const level = heading.tagName.replace("H", "");
        const element = document.createElement("a");
        // 设置 h1 标签的文本内容
        element.textContent = heading.textContent;
        element.href = '#item' + i;
        element.classList.add('menu-item')
        element.style.paddingLeft = (15 + 15 * (level - 1)) + 'px';
        element.addEventListener('click', () => {
            if (lastElement) {
                lastElement.style.color = 'black'
            }
            element.style.color = '#409EFF'
            lastElement = element;
        })
        aside.appendChild(element)
    }
}

doDrag();
buildMenu();